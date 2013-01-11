package arkco.sregex;

import java.io.*;
import java.util.*;

/**
 * <p>Title: 流数据多模式匹配</p>
 * <p>Description:
 * 流数据的正则表达式库是一个在数据流中进行正则匹配和搜索的工具，
 * 支持多模式匹配，通常运用在自动人机交互、网络通讯、大文本搜索
 * 等需要对流数据进行处理的应用中，如交换机自动操作程序、终端的
 * 命令自动执行
 * </p>
 * @author 肖海星 arksea@gmail.com
 * @version 1.1.6
 */
public class Matcher
{
    /**
     * @param input InputStream 输入流
     * @param output OutputStream 输出流，Matcher从InputStream读入的数据将被输出到output
     */
    public Matcher(InputStream input, OutputStream output)
    {
        inputStream = input;
        outputStream = output;
    }
    /**
     * @param input InputStream 输入流
     */
    public Matcher(InputStream input)
    {
        inputStream = input;
        outputStream = null;
    }
    /**
     * 将一个字符串作为输入串进行匹配
     * @param str String 待匹配串
     */
    public Matcher(String str)
    {
        inputStream = new ByteArrayInputStream(str.getBytes());
        outputStream = null;
    }
    /**
     * @param is boolean 设置为true时，Matcher将在读取数据超时时抛出TimeoutException;
     * 这个状态的初始值为false
     */
    public void setThrowTimeoutException(boolean is)
    {
        throwTimeoutException = is;
    }
    /**
     * @return boolean 返回Matcher是否会抛出TimeoutException
     */
    public boolean isThrowTimeoutException()
    {
        return throwTimeoutException;
    }
    /**
     * 设置默认超时时间
     * @param timeout long
     */
    public void setDefaultTimeout(long timeout)
    {
        defaultTimeout = timeout;
    }
    /**
     * multiLine为true时，模式'.'和'\s'将匹配换行符，所以".*"将会跨行匹配，
     * multiLine为false时则不进行跨行匹配，默认为true
     * @param multiLine boolean
     */
    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
    }

    /**
     * 返回默认超时时间
     * @return long
     */
    public long getDefaultTimeout()
    {
        return defaultTimeout;
    }

    private boolean throwTimeoutException = false;
    private InputStream inputStream;
    private OutputStream outputStream;
    private long defaultTimeout = 0;
    boolean isEOF = false;
    /**
     * 返回匹配值，这个值是一个二维数组
     * 例1、对于数据流 abcabcabcabc<br>
     * 模式 (abc)+<br>
     * 将得到结果：<br>
     * {<br>
     *   {"abcabcabcabc"},<br>
     *   {"abc","abc","abc"}<br>
     * }<br>
     * 例2、对于数据流<br>
     * liu 33 27 76 85<br>
     * wan 76 43 25 33<br>
     *
     * 模式 ((\w+)(?:\s(\d+))+\n)+<br>
     * 将得到结果：<br>
     * {<br>
     *   {"liu 33 27 76 85\nwan 76 43 25 33\n"},<br>
     *   {"liu 33 27 76 85\n","wan 76 43 25 33\n"},<br>
     *   {"liu","wan"},<br>
     *   {"33","27","76","85","76","43","25","33}<br>
     * }<br>
     * 而模式 ((\w+)\s(\d+)\s(\d+)\s(\d+)\s(\d+)\n)+<br>
     * 将的到结果：<br>
     * {<br>
     *   {"liu 33 27 76 85\nwan 76 43 25 33\n"},<br>
     *   {"liu 33 27 76 85\n","wan 76 43 25 33\n"},<br>
     *   {"liu","wan"},<br>
     *   {"33","76"},<br>
     *   {"27","43"},<br>
     *   {"76","25"},<br>
     *   {"85","33"}<br>
     * }<br>
     * @return Submatched[][]
     */
    public Submatched[][] getMatched()
    {
        return matchedResult;
    }
    /**
     * 在输入流中搜索匹配参数regex正则表达式的串
     * @param regex String 正则表达式<br>
     * @param timeout long 超时时间，与通常正则表达式引擎不同的地方，这是以流为数据源的独特特性<br>
     * @return int 返回 0 表示搜索到匹配的串，<br>
     *             当输入流结束时还没有搜索到匹配串将返回-1，<br>
     *             当超时未搜索到匹配串石，如果throwTimeoutException为false，也将返回-1<br>
     * @throws IOException
     * @throws EOFException 输入流结束时将抛出此异常
     * @throws TimeoutException
     * @throws PatternCompiler_E regex错误时抛出此异常
     * @throws InterruptedException
     */
    public int find(String regex,long timeout)
            throws IOException,EOFException,TimeoutException, PatternCompiler_E,InterruptedException
    {
        try
        {
            this.clear();
            this.add(regex);
            return find(timeout);
        }
        finally
        {
            this.clear();
        }
    }
    /**
     * 在输入流中搜索匹配多个正则表达式匹配的结果<br>
     * @param timeout long 超时时间
     * @param regexs String[] 多个正则表达式，相当于多次调用add(regex)
     * @return int
     * @throws IOException
     * @throws EOFException
     * @throws TimeoutException
     * @throws PatternCompiler_E
     * @throws InterruptedException
     */
    public int find(long timeout,String... regexs)
            throws IOException,EOFException,TimeoutException, PatternCompiler_E,InterruptedException
    {
        this.clear();
        for(String regex:regexs)
        {
            this.add(regex);
        }
        return find(timeout);
    }
    /**
     * @param regex String
     * @return int
     * @throws IOException
     * @throws EOFException
     * @throws TimeoutException
     * @throws PatternCompiler_E
     * @throws InterruptedException
     */
    public int find(String regex)
            throws IOException,EOFException,TimeoutException, PatternCompiler_E,InterruptedException
    {
        return find(regex,defaultTimeout);
    }
    /**
     * @param regexs String[]
     * @return int
     * @throws IOException
     * @throws EOFException
     * @throws TimeoutException
     * @throws PatternCompiler_E
     * @throws InterruptedException
     */
    public int find(String... regexs)
            throws IOException,EOFException,TimeoutException, PatternCompiler_E,InterruptedException
    {
        return find(defaultTimeout,regexs);
    }
    /**
     * @return int
     * @throws IOException
     * @throws EOFException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public int find()
        throws IOException,EOFException,TimeoutException,InterruptedException
    {
        return this.find(defaultTimeout);
    }
    /**
     * @param timeout long
     * @return int
     * @throws IOException
     * @throws EOFException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public int find(long timeout)
        throws IOException,EOFException,TimeoutException,InterruptedException
    {
        Iterator iter=patternList.iterator();
        while(iter.hasNext())
        {
            Pattern pat=(Pattern)iter.next();
            pat.clear();
        }
        int sleep = 50;
        int matchedIndex;
        int matchType=Pattern.NoMatched_M;
        while (!isClosed())
        {
            matchedIndex = 0;
            int ch = 0;
            if(isEOF) throw new EOFException();
            if (timeout == 0)
            {
                ch = inputStream.read();
            }
            else
            {
                long last = System.currentTimeMillis();
                while (inputStream.available() <= 0)
                {   if (System.currentTimeMillis() - last > timeout)
                    {
                        if (throwTimeoutException)
                        {
                            throw new TimeoutException();
                        }
                        else
                        {
                            return -1;
                        }
                    }
                    Thread.sleep(sleep);
                }
                ch = inputStream.read();
            }
            if (ch <= -1)
            {
                isEOF = true;
            }
            if (outputStream != null && ch>=0)
            {
                outputStream.write(ch);
                outputStream.flush();
            }
            iter=patternList.iterator();
            while(iter.hasNext())
            {
                Pattern pat=(Pattern)iter.next();
                matchType=pat.check(ch);
                if ( matchType == Pattern.Matched_M )
                {
                    matchedResult = pat.getMatchedResult();
                    return matchedIndex;
                }
                ++matchedIndex;
            }
        }
        throw new InterruptedException();
    }
    /**
     * 添加待匹配正则表达式。
     * @param pattern String
     * @throws PatternCompiler_E
     */
    public void add(String pattern)
        throws PatternCompiler_E
    {
        Pattern p=new Pattern(pattern);
        p.setMultiLine(multiLine);
        patternList.add(p);
    }
    /**
     * 添加待匹配正则表达式
     * @param pattern String
     * @param multiLine boolean
     * multiLine为true时，模式'.'和'\s'将匹配换行符，所以".*"将会跨行匹配，
     * multiLine为false时则不进行跨行匹配
     * @throws PatternCompiler_E
     */
    public void add(String pattern,boolean multiLine)
        throws PatternCompiler_E
    {
        Pattern p=new Pattern(pattern);
        p.setMultiLine(multiLine);
        patternList.add(p);
    }
    /**
     * 清除所有待匹配正则表达式
     */
    public void clear()
    {
        patternList.clear();
    }

    ArrayList patternList = new ArrayList();
    Submatched[][] matchedResult;
    volatile boolean closed=false;
    /**
     * @return boolean
     */
    public boolean isClosed()
    {
        return closed;
    }
    /**
     * @return boolean
     */
    public boolean isMultiLine() {
        return multiLine;
    }

    /**
     * 关闭Matcher,此操作将会使正在执行或之后执行的find()抛出InterruptedException
     */
    public void close()
    {
        closed=true;
    }
    boolean multiLine = true;
}
