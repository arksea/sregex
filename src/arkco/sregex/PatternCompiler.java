package arkco.sregex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;


import java.util.List;
import java.util.Iterator;

public class PatternCompiler
{
    final boolean DEBUG = true;
    final int EOF = -1;
    Stack groupBeginOffset;//Stack<Integer> groupBeginOffset;
    int[] pattern;
    Stack subCount;//Stack<LinkedList<Integer>> subCount;
    int offset;

    ArrayList codes;
    /**
     * groupCount被初始化为1，它总是比在正则表达式中扫描到的括号(对)数大1。
     * 这是因为它是用来确定需要提取的串的数量，
     * 而整个被匹配的串总是默认做为group[0]被提取出来。
     */
    int groupCount;
    int token;
    void reset()
    {
        offset = 0;
        groupCount=0;
        groupBeginOffset = new Stack();
        subCount=new Stack();
        codes = new ArrayList();
    }

    Codes compile(String regex)
        throws PatternCompiler_E
    {
        //将regex转换成int[]
        byte[] p = regex.getBytes();

        pattern = new int[p.length + 5];
        pattern[0]='(';
        for (int i = 0; i < p.length; ++i)
        {
            pattern[i+1] = p[i];
            if (pattern[i+1] < 0)
            {
                pattern[i+1] += 256;
            }
        }
        pattern[p.length + 1] = ')';
        pattern[p.length + 2] = EOF;
        pattern[p.length + 3] = EOF;
        pattern[p.length + 4] = EOF;

        reset();
        Code code;
        while (pattern[offset] != EOF)
        {
            code = getCode();
            if (code != null)
            {
                codes.add(code);
            }
        }
        if (groupBeginOffset.size() > 0)
        {
            throw new PatternCompiler_E("括号没有配对");
        }

        Codes codeObj = new Codes();
        codeObj.codes = new Code[codes.size()];
        Object[] objs = codes.toArray();
        for (int i = 0; i < codes.size(); ++i)
        {
            codeObj.codes[i] = (Code) objs[i];
        }
        codeObj.groupCount=groupCount;
        return codeObj;
    }

    int getToken()
        throws PatternCompiler_E
    {
        return getToken(Metacode.def); //默认元字符
    }

    int getToken(String meta)
        throws PatternCompiler_E
    {
        if (pattern[offset] == EOF)
        {
            return EOF;
        }
        if (meta.indexOf(pattern[offset]) == -1)
        {
            //ASCII字符
            token = pattern[offset];
            ++offset;
            return Token.CHAR;
        }
        int type;
        switch (pattern[offset])
        {
            case '.':
                token = CharType.ANY;
                type = Token.CHAR;
                ++offset;
                break;
            case '\\':
                switch (pattern[offset + 1])
                {
                    case '0':
                        token = CharType.EOF;
                        break;
                    case 'd':
                        token = CharType.DIGIT;
                        break;
                    case 'D':
                        token = CharType.NDIGIT;
                        break;
                    case 'w':
                        token = CharType.WORD;
                        break;
                    case 'W':
                        token = CharType.NWORD;
                        break;
                    case 's':
                        token = CharType.SPACE;
                        break;
                    case 'S':
                        token = CharType.NSPACE;
                        break;
                    case 'n':
                        token = '\n';
                        break;
                    case 'r':
                        token = '\r';
                        break;
                    case 't':
                        token = '\t';
                        break;
                    case 'x':
                        int x1=pattern[offset+2];
                        int x2=pattern[offset+3];
                        if(x1>='0' && x1<='9')
                        {
                            token = (x1-48)*16;
                        }
                        else if(x1>='A' && x1<='F')
                        {
                            token = (x1-55)*16;
                        }
                        else if(x1>='a' && x1<='f')
                        {
                            token = (x1-87)*16;
                        }
                        else
                        {
                            throw new PatternCompiler_E("(" + (offset+1) +
                                                ")：语法错误，\\x后应跟两位十六进制数");
                        }
                        if(x2>='0' && x2<='9')
                        {
                            token += (x2-48);
                        }
                        else if(x2>='A' && x2<='F')
                        {
                            token += (x2-55);
                        }
                        else if(x2>='a' && x2<='f')
                        {
                            token += (x2-87);
                        }
                        else
                        {
                            throw new PatternCompiler_E("(" + (offset+1) +
                                                ")：语法错误，\\x后应跟两位十六进制数");
                        }
                    ++offset;
                    ++offset;
                    break;
                    case EOF:
                        throw new PatternCompiler_E("(" + (offset+1) +
                                                ")：语法错误，转义符后缺少字符");
                    default:
                        token = pattern[offset + 1];
                        break;
                }
                ++offset;
                ++offset;
                type = Token.CHAR;
                break;
            case '*':
            case '+':
            case '?':
            case '{':
                token = pattern[offset];
                ++offset;
                type = Token.QUANTIFIER;
                break;
            default: //|()[^$
                token = pattern[offset];
                type = token;
                ++offset;
                break;
        }
        return type;
    }

    /**
     * 取{m,n}中的m或n
     * @return 取到数据则返回值，否则返回-1
     */
    int getCount()
    {
        try
        {
            StringBuffer buf = new StringBuffer();
            while (pattern[offset] <= '9' && pattern[offset] >= '0')
            {
                buf.append( (char) pattern[offset]);
                ++offset;
            }
            return Integer.parseInt(buf.toString());
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }

    void skipSpace()
    {
        while (pattern[offset] == ' ')
        {
            ++offset;
        }
    }

    Code getCode()
        throws PatternCompiler_E
    {
        if(pattern[offset] == EOF)
        {
            throw new RuntimeException("断言失败");
        }
        Code code = null;
        int type = getToken();
        switch (type)
        {
            case Token.CHAR:
                code = new MatchChar(token);
                break;
            case Token.QUANTIFIER:
                MatchCode preCode = null;
                try
                {
                    preCode = (MatchCode) codes.get(codes.size() - 1);
                }
                catch (ClassCastException e)
                {
                    throw new PatternCompiler_E("(" + offset +
                                                ")：语法错误，重复模式前没有合法的操作数");
                }
                if (preCode.repeat != null)
                {
                    throw new PatternCompiler_E("(" + offset +
                                                ")：语法错误，不允许有多个重复模式");
                }
                Repeat repeat = null;
                switch (token)
                {
                    case '*':
                        repeat = new Repeat(0, Integer.MAX_VALUE);
                        break;
                    case '+':
                        repeat = new Repeat(1, Integer.MAX_VALUE);
                        break;
                    case '?':
                        repeat = new Repeat(0, 1);
                        break;
                    case '{':
                        skipSpace();
                        int m = getCount();
                        if (m < 0)
                        {
                            throw new PatternCompiler_E("(" + offset +
                                ")：{m,n}模式错误");
                        }
                        skipSpace();
                        if (pattern[offset] == ',')
                        {
                            ++offset;
                            skipSpace();
                            int n = getCount();
                            if (n < 0)
                            { //{m,}
                                repeat = new Repeat(m, Integer.MAX_VALUE);
                            }
                            else
                            { //{m,n}
                                repeat = new Repeat(m, n);
                            }
                        }
                        else
                        { //{m}
                            repeat = new Repeat(m, m);
                        }
                        skipSpace();
                        if (pattern[offset] != '}')
                        {
                            throw new PatternCompiler_E("(" + offset +
                                ")缺少结束符}");
                        }
                        ++offset;
                        break;
                }
                preCode.repeat = repeat;
                if (preCode.type == Code.GROUP_END)
                {
                    //设置相应GroupBegin.repeat
                    ( (GroupBegin) codes.get( ( (GroupEnd) preCode).beginOffset)).
                        repeat = repeat;
                }
                break;
            case '[':
                IntArray chars = new IntArray();
                boolean reverse = false;
                if (pattern[offset] == '^')
                {
                    ++offset;
                    reverse = true;
                    type = getToken(Metacode.set);
                }
                else
                {
                    type = getToken(Metacode.set);
                }
                do
                {
                    if (type != Token.CHAR)
                    {
                        throw new PatternCompiler_E("(" + offset +
                            ")字符集中包含非法字符");
                    }
                    int begin = token;
                    if (pattern[offset] == '-')
                    {
                        ++offset;
                        type = getToken(Metacode.set);
                        if (type != Token.CHAR)
                        {
                            throw new PatternCompiler_E("(" + offset +
                                ")字符集标记-后语法错误");
                        }
                        if (token <= begin)
                        {
                            throw new PatternCompiler_E("(" + offset +
                                ")字符集标记-后的字符应比之前的字符大");
                        }
                        for (int i = begin; i <= token; ++i)
                        {
                            chars.put(i);
                        }
                    }
                    else
                    {
                        chars.put(token);
                    }
                    type = getToken(Metacode.set);
                } while (type != ']');
                code = new MatchCharSet(reverse, chars.getArray());
                break;
            case '|':
                OR or = new OR();
                if(groupBeginOffset.size()>0)
                {
                    or.groupOffset=((Integer)groupBeginOffset.peek()).intValue();
                }
                GroupBegin gb=(GroupBegin)codes.get(or.groupOffset);
                ((List)subCount.peek()).add(new Integer(codes.size()+1));
                code = or;
                break;
            case '(':
                code = new GroupBegin();
                gb = (GroupBegin) code;
                subCount.push(new LinkedList());
                if(pattern[offset]=='?' && pattern[offset+1]==':')
                {
                    offset+=2;
                    gb.pick=false;
                }
                gb.index=groupCount++;
                groupBeginOffset.push(new Integer(codes.size()));
                break;
            case ')':
                code = new GroupEnd();
                try
                {
                    int begin = ((Integer)groupBeginOffset.pop()).intValue();
                    gb = (GroupBegin) codes.get(begin);
                    List sub=(List)subCount.pop();
                    gb.sub=new int[sub.size()];
                    int i=0;
                    Iterator iter=sub.iterator();
                    while(iter.hasNext())
                    {
                        int c=((Integer)iter.next()).intValue();
                        gb.sub[i]=c;
                        ++i;
                    }
                    ( (GroupEnd) code).beginOffset = begin;
                    gb.endOffset = codes.size();
                    ((GroupEnd) code).index=gb.index;
                    ((GroupEnd) code).pick=gb.pick;
                }
                catch (Exception e)
                {
                    throw new PatternCompiler_E("位置(" + offset + "): 括号没有配对",e);
                }
                break;
            case '^':
                code = new LineHead();
                break;
            case '$':
                code = new LineTail();
                break;
            default:
                throw new PatternCompiler_E("位置(" + offset + "): 语法错误");
        }
        return code;
    }

    public static void main(String[] argv)
        throws Exception
    {
        PatternCompiler compiler = new PatternCompiler();
        compiler.compile("(ABCD\\d+|1234\\w+)+hello\\d+|world\\d+");
    }

}

//正则表达式中的标记
final class Token
{
    static final int
        CHAR = 0, //ASCII 0--255,\d,\D,\s,\S,\w,\W,.
        QUANTIFIER = 1; //* ? + {m,n} {m} {m,}
    //| [ ] () ^ $ 直接用字符表示
}
