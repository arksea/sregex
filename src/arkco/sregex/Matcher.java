package arkco.sregex;

import java.io.*;
import java.util.*;

/**
 * <p>Title: �����ݶ�ģʽƥ��</p>
 * <p>Description:
 * �����ݵ�������ʽ����һ�����������н�������ƥ��������Ĺ��ߣ�
 * ֧�ֶ�ģʽƥ�䣬ͨ���������Զ��˻�����������ͨѶ�����ı�����
 * ����Ҫ�������ݽ��д����Ӧ���У��罻�����Զ����������ն˵�
 * �����Զ�ִ��
 * </p>
 * @author Ф���� arksea@gmail.com
 * @version 1.1.6
 */
public class Matcher
{
    /**
     * @param input InputStream ������
     * @param output OutputStream �������Matcher��InputStream��������ݽ��������output
     */
    public Matcher(InputStream input, OutputStream output)
    {
        inputStream = input;
        outputStream = output;
    }
    /**
     * @param input InputStream ������
     */
    public Matcher(InputStream input)
    {
        inputStream = input;
        outputStream = null;
    }
    /**
     * ��һ���ַ�����Ϊ���봮����ƥ��
     * @param str String ��ƥ�䴮
     */
    public Matcher(String str)
    {
        inputStream = new ByteArrayInputStream(str.getBytes());
        outputStream = null;
    }
    /**
     * @param is boolean ����Ϊtrueʱ��Matcher���ڶ�ȡ���ݳ�ʱʱ�׳�TimeoutException;
     * ���״̬�ĳ�ʼֵΪfalse
     */
    public void setThrowTimeoutException(boolean is)
    {
        throwTimeoutException = is;
    }
    /**
     * @return boolean ����Matcher�Ƿ���׳�TimeoutException
     */
    public boolean isThrowTimeoutException()
    {
        return throwTimeoutException;
    }
    /**
     * ����Ĭ�ϳ�ʱʱ��
     * @param timeout long
     */
    public void setDefaultTimeout(long timeout)
    {
        defaultTimeout = timeout;
    }
    /**
     * multiLineΪtrueʱ��ģʽ'.'��'\s'��ƥ�任�з�������".*"�������ƥ�䣬
     * multiLineΪfalseʱ�򲻽��п���ƥ�䣬Ĭ��Ϊtrue
     * @param multiLine boolean
     */
    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
    }

    /**
     * ����Ĭ�ϳ�ʱʱ��
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
     * ����ƥ��ֵ�����ֵ��һ����ά����
     * ��1������������ abcabcabcabc<br>
     * ģʽ (abc)+<br>
     * ���õ������<br>
     * {<br>
     *   {"abcabcabcabc"},<br>
     *   {"abc","abc","abc"}<br>
     * }<br>
     * ��2������������<br>
     * liu 33 27 76 85<br>
     * wan 76 43 25 33<br>
     *
     * ģʽ ((\w+)(?:\s(\d+))+\n)+<br>
     * ���õ������<br>
     * {<br>
     *   {"liu 33 27 76 85\nwan 76 43 25 33\n"},<br>
     *   {"liu 33 27 76 85\n","wan 76 43 25 33\n"},<br>
     *   {"liu","wan"},<br>
     *   {"33","27","76","85","76","43","25","33}<br>
     * }<br>
     * ��ģʽ ((\w+)\s(\d+)\s(\d+)\s(\d+)\s(\d+)\n)+<br>
     * ���ĵ������<br>
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
     * ��������������ƥ�����regex������ʽ�Ĵ�
     * @param regex String ������ʽ<br>
     * @param timeout long ��ʱʱ�䣬��ͨ��������ʽ���治ͬ�ĵط�����������Ϊ����Դ�Ķ�������<br>
     * @return int ���� 0 ��ʾ������ƥ��Ĵ���<br>
     *             ������������ʱ��û��������ƥ�䴮������-1��<br>
     *             ����ʱδ������ƥ�䴮ʯ�����throwTimeoutExceptionΪfalse��Ҳ������-1<br>
     * @throws IOException
     * @throws EOFException ����������ʱ���׳����쳣
     * @throws TimeoutException
     * @throws PatternCompiler_E regex����ʱ�׳����쳣
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
     * ��������������ƥ����������ʽƥ��Ľ��<br>
     * @param timeout long ��ʱʱ��
     * @param regexs String[] ���������ʽ���൱�ڶ�ε���add(regex)
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
     * ��Ӵ�ƥ��������ʽ��
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
     * ��Ӵ�ƥ��������ʽ
     * @param pattern String
     * @param multiLine boolean
     * multiLineΪtrueʱ��ģʽ'.'��'\s'��ƥ�任�з�������".*"�������ƥ�䣬
     * multiLineΪfalseʱ�򲻽��п���ƥ��
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
     * ������д�ƥ��������ʽ
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
     * �ر�Matcher,�˲�������ʹ����ִ�л�֮��ִ�е�find()�׳�InterruptedException
     */
    public void close()
    {
        closed=true;
    }
    boolean multiLine = true;
}
