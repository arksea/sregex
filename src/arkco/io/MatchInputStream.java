package arkco.io;

import java.io.InputStream;
import java.io.IOException;
import arkco.sregex.Pattern;
import java.util.ArrayList;
import arkco.sregex.PatternCompiler_E;
import java.util.Iterator;
import arkco.sregex.*;

/**
 * <p>Title: ֧�ֶ�ģʽƥ���InputStream</p>
 *
 * <p>Description:<br>
 * arkco.io.MatcheInputStream��Ϊ����������Ӧ��׼���ģ�<br>
 * ������������������ָ�������ݽ��ᴥ��IMatchedEvent�¼���<br>
 * IMatchedEvent�¼�����ƥ������ݹ�������<br>
 * ���ӣ�<br>
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
 *       ma.add(��height=(\d+)��);//id=0<br>
 *       ma.add(��weight=(\d+)��);//id=1<br>
 *       while(ma.read()>=0){}<br>
 * }  }<br>
 * </code>
 * </p>
 *
 * @author Ф���� arksea@gmail.com
 * @version 1.1.6
 */
public class MatchInputStream extends InputStream
{
    /**
     * @param input InputStream ������
     * @param event IMatchedEvent �������¼�
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
     * ���ó�ʱʱ�䣬��λΪ����
     * @param timeout long
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }
    /**
     * ȡ��ʱʱ�䣬��λΪ����
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
     * Ϊtrueʱ����ƥ��һ��ģʽ���������ģʽ��״̬<br>
     * Ϊfalseʱ����ƥ��һ��ģʽ��������ǰģʽ��״̬<br>
     * ���ӣ�<br>
     * MatchInputStream mat=new MatcherInputStream(in);<br>
     * mat.add("\w+ world");<br>
     * mat.add("hello");<br>
     * �������ַ�����2007,hello world!��ʱ�����resetAllPatternOnMatchedΪtrue��<br>
     * ��ֻƥ��ģʽ"hello"����Ϊƥ���˴�ģʽ�󽫻�����ģʽ��\w+ world��֮ǰ�Ѳ���ƥ���״̬��<br>
     * ���Ϊfalse������ģʽ����ƥ�䣬�Ȼ��������MatchedEvent�¼�<br>
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
     * ��Ӵ�ƥ��ģʽ
     * @param pattern String
     * @throws PatternCompiler_E
     */
    public void add(String pattern)
        throws PatternCompiler_E
    {
        patternList.add(new Pattern(pattern));
    }
    /**
     * ������д�ƥ��ģʽ
     */
    public void clear()
    {
        patternList.clear();
    }
}
