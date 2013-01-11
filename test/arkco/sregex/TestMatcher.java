package arkco.sregex;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

public class TestMatcher extends TestCase
{
    /**
     * @directed
     */
    private Matcher matcher = null;
    private static int testIndex = 1;
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    protected void tearDown()
        throws Exception
    {
        matcher = null;
        super.tearDown();
    }

    private void init(String str, String regex)
    {
        System.out.println("\n[TEST" + testIndex
                           +
                           "]#################################################\n");
        ++testIndex;
        System.out.println(str);
        InputStream in = new BufferedInputStream(
            new ByteArrayInputStream(str.getBytes()));
        OutputStream out = new ByteArrayOutputStream();
        matcher = new Matcher(in, out);
        try
        {
            matcher.add(regex);
        }
        catch (PatternCompiler_E ex)
        {
            ex.printStackTrace();
            assertTrue("正则表达式错误", false);
        }
    }

    private int find()
    {
        int ret=-1;
        try
        {
            ret=matcher.find();
            if(ret<0) return ret;
            Submatched[][] groups = matcher.getMatched();
            int index = 0;
            for(int i=0;i<groups.length;++i)
            {
                Submatched[] m=groups[i];
                System.out.println("[GROUP" + index +
                                   "]--------------------------------------------------");
                ++index;
                for(int j=0;j<m.length;++j)
                {
                    System.out.print('[');
                    System.out.print(m[j].toString());
                    System.out.print(']');
                }
                System.out.print("\n");
            }
        }
        catch (EOFException ex)
        {
            System.out.println("not matched");
            //this.assertTrue(false);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            this.assertTrue(false);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }

        return ret;
    }

    public void test_base1()
        throws PatternCompiler_E, IOException
    { //字符
        init("..1\n\rL \t?a\r", ".\\D\\d\\n\\r\\S\\s\\t\\W\\w\\x0D");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "..1\n\rL \t?a\r");
    }

    public void test_base2()
        throws PatternCompiler_E, IOException
    { //字符类
        init("Here is thie text.", "[aeiou]");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "e");

        init("#####888Here is the text.#####", "[\\w\\s\\.]+#");
        find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "888Here is the text.#");

        init("#####888Here is the text.#####", "[A-Za-z\\s\\.]+#");
        find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "Here is the text.#");
    }

    public void test_base3()
        throws PatternCompiler_E, IOException
    { //或
        init("ls\nuser home\nquit\nbye bye!\n", "exit|quit|stop");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "quit");
    }

    public void test_base4()
        throws PatternCompiler_E, IOException
    { //量词
        init("It's NO.3!", "NO.\\d?!");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "NO.3!");

        init("It's NO.!It's NO.1!It's NO.123!\n", "(It's NO.\\d*!)+\n");
        find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(),
                     "It's NO.!It's NO.1!It's NO.123!\n");
        assertEquals(groups[1].length, 3);
        assertEquals(groups[1][0].toString(), "It's NO.!");
        assertEquals(groups[1][1].toString(), "It's NO.1!");
        assertEquals(groups[1][2].toString(), "It's NO.123!");
    }

    public void test_base5()
        throws PatternCompiler_E, IOException
    { //^$
        init("########\n abcd1234\nefgh1234\n########\n", "(^\\w+$)\\s+");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "efgh1234\n");
        assertEquals(groups[0].length, 1);
        assertEquals(groups[1][0].toString(), "efgh1234");

        init("########\n abcd1234\nefgh1234 ########\n", "(^\\w+)\\s+");
        find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "efgh1234 ");
        assertEquals(groups[0].length, 1);
        assertEquals(groups[1][0].toString(), "efgh1234");

        init("########\n abcd1234\nefgh1234\n########\n", "^(\\w+$)\\s+");
        find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "efgh1234\n");
        assertEquals(groups[0].length, 1);
        assertEquals(groups[1][0].toString(), "efgh1234");

    }

    /////////////////////////////////////////////////////////////////
    public void test15()
        throws IOException
    {
        String str="ch\nzh\n";
        this.init(str ,"(?:(\\w+)\n)+");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "ch\nzh\n");
        assertEquals(groups[1].length, 2);
        assertEquals(groups[1][0].toString(), "ch");
        assertEquals(groups[1][1].toString(), "zh");

        str="ch\nzh\n######";
        this.init(str ,"(?:(\\w+)\n)+");
        this.find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "ch\nzh\n");
        assertEquals(groups[1].length, 2);
        assertEquals(groups[1][0].toString(), "ch");
        assertEquals(groups[1][1].toString(), "zh");

        str="ABC\nABC\n######";
        this.init(str ,"(?:(ABC)\n)+");
        this.find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "ABC\nABC\n");
        assertEquals(groups[1].length, 2);
        assertEquals(groups[1][0].toString(), "ABC");
        assertEquals(groups[1][1].toString(), "ABC");
        str="ABC\nABC\n######";
        this.init(str ,"(?:(ABC)\n)+#");
        this.find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "ABC\nABC\n#");
        assertEquals(groups[1].length, 2);
        assertEquals(groups[1][0].toString(), "ABC");
        assertEquals(groups[1][1].toString(), "ABC");


    }

    public void test16()
        throws IOException
    {
        this.init("51516", "(5\\d+)+6");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "51516");
        assertEquals(groups[1].length, 2);
        assertEquals(groups[1][0].toString(), "51");
        assertEquals(groups[1][1].toString(), "51");
    }

    public void test17()
        throws IOException
    {
        this.init("ab de ;", "(?:(\\w+)\\s+)+;");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "ab de ;");
        assertEquals(groups[1].length, 2);
        assertEquals(groups[1][0].toString(), "ab");
        assertEquals(groups[1][1].toString(), "de");
    }

    public void test18()
        throws IOException
    {
        this.init("nnnabcabcdmnomnonnnabcdefgabcabcdmnomnabcabcabcdmnomno;",
                  "\\w+(abc)+d(mno)+;");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 3);
        assertEquals(groups[0][0].toString(),
                     "nnnabcabcdmnomnonnnabcdefgabcabcdmnomnabcabcabcdmnomno;");
        assertEquals(groups[1].length, 3);
        for (int i = 0; i < 3; ++i)
        {
            assertEquals(groups[1][i].toString(), "abc");
        }
        assertEquals(groups[2].length, 2);
        for (int i = 0; i < 2; ++i)
        {
            assertEquals(groups[2][i].toString(), "mno");
        }
    }

    public void test19()
        throws IOException
    {
        this.init("abcabcabcabc;", "(abc)+;");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "abcabcabcabc;");
        assertEquals(groups[1].length, 4);
        for (int i = 0; i < 4; ++i)
        {
            assertEquals(groups[1][i].toString(), "abc");
        }
        //////////////////////////////////////////////////////
        this.init("abcabcabcabc", "(abc)+");
        this.find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "abcabcabcabc");
        assertEquals(groups[1].length, 4);
        for (int i = 0; i < 4; ++i)
        {
            assertEquals(groups[1][i].toString(), "abc");
        }
        //////////////////////////////////////////////////////
        this.init("abcabc;", "(abc)+");
        this.find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "abcabc");
        assertEquals(groups[1].length, 2);
        for (int i = 0; i < 2; ++i)
        {
            assertEquals(groups[1][i].toString(), "abc");
        }

    }
    public void test20()
        throws IOException
    {

    }
    public void test21()
        throws IOException
    {
        this.init("AB5773", "(ABC)*577");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        String str = groups[0][0].toString();
        System.out.println("test6: " + str);
        assertEquals(groups[0][0].toString(), "577");
        assertEquals(groups[1].length, 0);
    }

    public void test22()
        throws IOException
    {
        this.init("532154357756654", "\\d*577");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "5321543577");
    }

    public void test23()
        throws IOException
    {
        this.init("TTTTABCABCABTTTE", "TT(ABC)*ABT+");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "TTABCABCABTTT");
        assertEquals(groups[1].length, 2);
        assertEquals(groups[1][0].toString(), "ABC");
        assertEquals(groups[1][1].toString(), "ABC");
    }

    public void test24()
        throws IOException
    {
        this.init("0", "\\d");
        this.find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "0");
    }

    public void test25()
        throws PatternCompiler_E, IOException
    { //边界测试.匹配'*'
        init("*", "\\*");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "*");
    }

    public void test26()
        throws PatternCompiler_E, IOException
    { //边界测试.匹配'\D'
        init("\\", "\\D");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "\\");
    }

    public void test27()
        throws PatternCompiler_E, IOException
    { //边界测试.匹配'\D+'
        init("_ad\\", "\\D+\\\\");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "_ad\\");
    }

    public void test28()
        throws PatternCompiler_E, IOException
    { //边界测试.匹配'\*+'
        init("*********1", "\\*+");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "*********");
    }

    public void test29()
        throws PatternCompiler_E, IOException
    { //边界测试.测试正则表达式的各个特殊字符
        init("t423tredf*~+~?~.~|~3~a~\r~\n~ ~&~_~^~fdafd",
             "\\*~\\+~\\?~\\.~\\|~\\d~\\D~\\r~\\n~\\s~\\S~\\w~\\W~");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "*~+~?~.~|~3~a~\r~\n~ ~&~_~^~");
    }

    public void test30()
        throws PatternCompiler_E, IOException
    {
        init("1234", "abcd|1234");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "1234");
    }

    public void test31()
        throws PatternCompiler_E, IOException
    {
        init("abcd123;", "(\\d+|\\w+);");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "abcd123;");
        assertEquals(groups[1].length, 1);
        assertEquals(groups[1][0].toString(), "abcd123");
    }

    public void test32()
        throws PatternCompiler_E, IOException
    {
        init("abcd123;", "(\\d+|\\W+)");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "123");
        assertEquals(groups[1].length, 1);
        assertEquals(groups[1][0].toString(), "123");
    }

    public void test33()
        throws PatternCompiler_E, IOException
    {
        init("abc789def123ghi", "(\\d+|[a-z]+)+");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "abc789def123ghi");
        assertEquals(groups[1].length, 5);
        assertEquals(groups[1][0].toString(), "abc");
        assertEquals(groups[1][1].toString(), "789");
        assertEquals(groups[1][2].toString(), "def");
        assertEquals(groups[1][3].toString(), "123");
        assertEquals(groups[1][4].toString(), "ghi");
        ///////////////////////////////////////
        init("ab78cd;", "(\\d+|[a-z]+)+");
        find();
        groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "ab78cd");
        assertEquals(groups[1].length, 3);
        assertEquals(groups[1][0].toString(), "ab");
        assertEquals(groups[1][1].toString(), "78");
        assertEquals(groups[1][2].toString(), "cd");
    }

    public void test35()
        throws PatternCompiler_E, IOException
    {
        init("abcabc", "((abc)*)+");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 3);
        assertEquals(groups[0][0].toString(), "abcabc");
        assertEquals(groups[1].length, 1);
        assertEquals(groups[1][0].toString(), "abcabc");
        assertEquals(groups[2].length, 2);
        assertEquals(groups[2][0].toString(), "abc");
        assertEquals(groups[2][1].toString(), "abc");
        ///////////////////////////////////////////////////
    }

    public void test36()
        throws PatternCompiler_E, IOException
    {
        init("abcabc", "((abc)+)*");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 3);
        assertEquals(groups[0][0].toString(), "abcabc");
        assertEquals(groups[1].length, 1);
        assertEquals(groups[1][0].toString(), "abcabc");
        assertEquals(groups[2].length, 2);
        assertEquals(groups[2][0].toString(), "abc");
        assertEquals(groups[2][1].toString(), "abc");
    }

    public void test37()
        throws PatternCompiler_E, IOException
    {
        init("abcdefg1234576_xyz.", "(\\w+)\\W");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "abcdefg1234576_xyz.");
        assertEquals(groups[1].length, 1);
        assertEquals(groups[1][0].toString(), "abcdefg1234576_xyz");
    }

    public void test38()
        throws PatternCompiler_E, IOException
    {
        init("成功登录\n--- end", "成功登录\\s+--- end");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "成功登录\n--- end");
    }

    public void test39()
        throws PatternCompiler_E, IOException
    {
        init("name=John;height=173;weight=63;", "height=\\d+");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "height=173");
    }

    public void test40()
        throws PatternCompiler_E, IOException
    {
        init("A ark@134.128.5.237 login","\\w+@[\\d+\\.]+");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0][0].toString(), "ark@134.128.5.237");
    }

    public void test41()
        throws PatternCompiler_E, IOException
    {
        init("* @author Hong","(@\\w+)*");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0][0].toString(), "@author");
        assertEquals(groups[1][0].toString(), "@author");
    }

    public void test42()
        throws PatternCompiler_E, IOException
    {
        init(" 123 456 789","(?:\\s+(\\d+)){3}");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[1].length, 3);
        assertEquals(groups[1][0].toString(), "123");
        assertEquals(groups[1][1].toString(), "456");
        assertEquals(groups[1][2].toString(), "789");
    }

    public void test43()
        throws PatternCompiler_E, IOException
    {
        String str="fdsfd\nfdasfd\nfdsfdf\n 0  0  18496  46292 148448 445248    0    0     0   108  130   235  2  6 92  83";
        init(str,"(?:.*\\n){3}(?:\\s+(\\d+)){16}");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[1].length, 16);
        assertEquals(groups[1][0].toString(), "0");
        assertEquals(groups[1][1].toString(), "0");
        assertEquals(groups[1][2].toString(), "18496");
        assertEquals(groups[1][15].toString(), "83");
    }

    public void test44()
        throws PatternCompiler_E, IOException
    {
        String str=" 0.02  0.13 0.15";
        init(str,"(\\S+)\\s+(\\S+)\\s+(\\S+)\\0");
//        matcher.setDefaultTimeout(500);
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 4);
        assertEquals(groups[1][0].toString(), "0.02");
        assertEquals(groups[2][0].toString(), "0.13");
        assertEquals(groups[3][0].toString(), "0.15");
    }

    public void test45()
        throws PatternCompiler_E, IOException
    {
        String str="+++    HW_SHLR                                  2004-07-05 13:17:11";
        init(str,"(\\+{3})\\s+\\S+\\s+\\d{4}-\\d{2}-\\d{2}\\s+(\\d{2}:\\d{2}:\\d{2})");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 3);
        assertEquals(groups[1][0].toString(), "+++");
        assertEquals(groups[2][0].toString(), "13:17:11");
    }


    public void test46()
        throws PatternCompiler_E, IOException
    {
        String str="+++    HW_SHLR                                  2004-07-05 13:17:11\n"+
                   "ALARM  297600 ";
        init(str,"(\\+{3})\\s+\\S+\\s+\\d{4}-\\d{2}-\\d{2}\\s+(\\d{2}:\\d{2}:\\d{2})\\nALARM\\s+(\\d+)");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 4);
        assertEquals(groups[1][0].toString(), "+++");
        assertEquals(groups[2][0].toString(), "13:17:11");
        assertEquals(groups[3][0].toString(), "297600");
    }

    public void test47()
        throws PatternCompiler_E, IOException
    {
        String str="ALARM\n"+
                   "a  =  1     b  =  2    c  =  3   d  =  4 hello\n"+
                   "e  =  5     f  =  6    g  =  7   h  =  8 world\n  ---END";

        init(str,"ALARM\\s+(((\\w+)\\s*=\\s*(\\d+)\\s+)+\\w+\\s+)+---END");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 5);
        assertEquals(groups[3].length, 8);
        assertEquals(groups[3][0].toString(), "a");
        assertEquals(groups[3][7].toString(), "h");
        assertEquals(groups[4].length, 8);
        assertEquals(groups[4][0].toString(), "1");
        assertEquals(groups[4][7].toString(), "8");
    }

    public void test48()
        throws PatternCompiler_E, IOException
    {
        String str=" 2566     1   2 syslogd          syslogd -m 26.4 root Jan31 24 S 00:00:51     13.5 root     Jan31  24 S 00:00:51\n"+
                   " 2848     1   0 gpm              gpm -t imps2 -m   0.0 root     Jan31  24 S 00:00:01\n";
        init(str,"(\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\w+)\\s+(\\S.*)\\s+(\\d+\\.\\d+)\\s+(\\w+)\\s+(\\w+)\\s+\\d+\\s\\w\\s(\\d\\d:\\d\\d:\\d\\d)\\n)+\\0");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 11);
        assertEquals(groups[7][0].toString(), "13.5");
    }

    public void test49()
            throws PatternCompiler_E, IOException
    {
        String str=  "  Bundle \n"+
                     "  HPUXBase-Aux       B.11.11.0309    HP-UX Base OS Auxiliary\n"+
                     "  HWEnabT-le11i      B.11.11.0309.4  Hardware Enablement Patches for HP-UX 11i, September 2003\n"+
                     "  IEther-00          B.11.11.03      PCI Ethernet;Supptd HW=A6974A \n";
        init(str,"(\\S+)\\s+(\\S+\\.\\d+\\.\\d+)\\s+(\\S.*)\\n");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(4,groups.length);
        assertEquals("HPUXBase-Aux       B.11.11.0309    HP-UX Base OS Auxiliary\n",groups[0][0].toString());
        assertEquals("HPUXBase-Aux",groups[1][0].toString());
        assertEquals("B.11.11.0309",groups[2][0].toString());
        assertEquals("HP-UX Base OS Auxiliary",groups[3][0].toString());
    }

    public void test50()
            throws PatternCompiler_E, IOException
    {
        init("app.log.20070709","app\\.log\\.\\d+(-\\d+)?");
        find();
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 2);
        assertEquals(groups[0].length, 1);
        assertEquals(groups[0][0].toString(), "app.log.20070709");
        assertEquals(groups[1].length, 0);
    }

    public void test51()
            throws PatternCompiler_E, IOException
    {
        init("app.log.20070709-1752353","app\\.log\\.\\d+(?:-\\d+)?");
        assertEquals(0,find());
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 1);
        assertEquals(groups[0].length, 1);
        assertEquals(groups[0][0].toString(), "app.log.20070709-1752353");
    }

    public void test52()
            throws PatternCompiler_E, IOException
    {
        init("\n","app\\.log\\.\\d+(?:-\\d+)?");
        assertEquals(-1,find());
    }
    public void test53()
    {
        init("liu 33 27 76 85\nwan 76 43 25 33\n","((\\w+)(?:\\s(\\d+)){4}\n)+");
        assertEquals(0,find());
        Submatched[][] groups = matcher.getMatched();
        assertEquals(groups.length, 4);
        assertEquals(groups[1].length, 2);
        assertEquals(groups[1][0].toString(), "liu 33 27 76 85\n");
        assertEquals(groups[1][1].toString(), "wan 76 43 25 33\n");
        assertEquals(groups[2].length, 2);
        assertEquals(groups[2][0].toString(), "liu");
        assertEquals(groups[2][1].toString(), "wan");
        assertEquals(groups[3].length, 8);
        assertEquals(groups[3][0].toString(), "33");
        assertEquals(groups[3][1].toString(), "27");
        assertEquals(groups[3][2].toString(), "76");
        assertEquals(groups[3][3].toString(), "85");
        assertEquals(groups[3][4].toString(), "76");
        assertEquals(groups[3][5].toString(), "43");
        assertEquals(groups[3][6].toString(), "25");
        assertEquals(groups[3][7].toString(), "33");
    }

    public void test54()
            throws PatternCompiler_E, IOException
    {
        String reg="^\\w+\n(\\s+.*\n)*\\s+student\n(\\s+(\\d+)\n){3}";
        init("\nPoint\n   66\n   32\nAlbert\n  ...\n  多行的简介,比如特长、喜好等\n  ...\n   student\n   173\n   16\n   62\n",reg);
        matcher.clear();
        matcher.add(reg,false);
        assertEquals(0,find());
    }
}
