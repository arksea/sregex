package arkco.io;

import java.io.InputStream;
import java.io.IOException;
import arkco.sregex.Pattern;
import java.util.ArrayList;
import arkco.sregex.PatternCompiler_E;
import java.util.Iterator;
import arkco.sregex.*;

/**
 * <p>Title: 支持多模式匹配的InputStream</p>
 *
 * <p>Description:<br>
 * arkco.io.MatcheInputStream是为数据驱动的应用准备的，<br>
 * 当在数据流中搜索到指定的数据将会触发IMatchedEvent事件，<br>
 * IMatchedEvent事件传递匹配的数据供程序处理。<br>
 * 例子：<br>
 * <code>
 * class Handler implements IMatchedEvent<br>
 * {  public void onMatched(Submatched[][] matched,int id)<br>
 *    {  switch(id)<br>
 *       {  case 0:<br>
 *             isHeight(matched[1][0]);<br>
 *             break;<br>
 *          case 1:<br>
 *             isWeight(matched[1][0]);<br>
 *             break;<br>
 * }  }  }<br>
 * class Test<br>
 * {  ...<br>
 *    InputStream inputStream;<br>
 *    public void run()<br>
 *    {  MatcheInputStream ma = new MatcheInputStream(inputStream,new Handler());<br>
 *       ma.add(“height=(\d+)”);//id=0<br>
 *       ma.add(“weight=(\d+)”);//id=1<br>
 *       while(ma.read()>=0){}<br>
 * }  }<br>
 * </code>
 * </p>
 *
 * @author 肖海星 arksea@gmail.com
 * @version 1.1.6
 */
public class MatchInputStream extends InputStream
{
    /**
     * @param input InputStream 输入流
     * @param event IMatchedEvent 待触发事件
     */
    public MatchInputStream(InputStream input, IMatchedEvent event)
    {
        this.inputStream = input;
        this.event = event;
    }

    private InputStream inputStream;
    private IMatchedEvent event;
    long timeout = 0;
    /**
     * 设置超时时间，单位为毫秒
     * @param timeout long
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }
    /**
     * 取超时时间，单位为毫秒
     * @param timeout long
     * @return long
     */
    public long getTimeout(long timeout)
    {
        return this.timeout;
    }
    /**
     * @return int
     * @throws IOException
     * @throws TimeoutException
     */
    public int read()
        throws IOException,TimeoutException
    {
        int ch;
        int sleep = 20;
        if (timeout == 0)
        {
            ch = inputStream.read();
        }
        else
        {
            long last = System.currentTimeMillis();
            while (inputStream.available() <= 0)
            {
                if (System.currentTimeMillis() - last > timeout)
                {
                    throw new TimeoutException();
                }
                try
                {
                    Thread.sleep(sleep);
                }
                catch (InterruptedException ex)
                {}
            }
            ch = inputStream.read();
        }

        if (ch <= -1)
        {
            return -1;
        }
        int index = 0;
        Iterator iter=patternList.iterator();
        while(iter.hasNext())
        {
            Pattern pat=(Pattern)iter.next();
            if (pat.check(ch) == Pattern.Matched_M)
            {
                event.onMatched(pat.getMatchedResult(), index);
                if (!resetAllPatternOnMatched)
                {
                    pat.clear();
                    break;
                }
                Iterator iterClear=patternList.iterator();
                while(iterClear.hasNext())
                {
                    Pattern patClear=(Pattern)iterClear.next();
                    patClear.clear();
                }
            }
            ++index;
        }
        return ch;
    }

    /**
     * 为true时，当匹配一个模式后将清除所有模式的状态<br>
     * 为false时，当匹配一个模式后仅清除当前模式的状态<br>
     * 例子：<br>
     * MatchInputStream mat=new MatcherInputStream(in);<br>
     * mat.add("\w+ world");<br>
     * mat.add("hello");<br>
     * 当输入字符串“2007,hello world!”时，如果resetAllPatternOnMatched为true，<br>
     * 将只匹配模式"hello"，因为匹配了此模式后将会重置模式“\w+ world”之前已部分匹配的状态，<br>
     * 如果为false，则两模式都会匹配，既会产生两次MatchedEvent事件<br>
     * @param is boolean
     */
    public void setResetAllPatternOnMatched(boolean is)
    {
        resetAllPatternOnMatched = is;
    }

    private boolean resetAllPatternOnMatched = false;
    /**
     * @return boolean
     */
    public boolean isResetAllPatternOnMatched()
    {
        return resetAllPatternOnMatched;
    }

    ArrayList patternList = new ArrayList();
    /**
     * 添加待匹配模式
     * @param pattern String
     * @throws PatternCompiler_E
     */
    public void add(String pattern)
        throws PatternCompiler_E
    {
        patternList.add(new Pattern(pattern));
    }
    /**
     * 清除所有待匹配模式
     */
    public void clear()
    {
        patternList.clear();
    }
}
