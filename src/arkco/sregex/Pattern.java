package arkco.sregex;

import java.util.*;

/**
 * <p>Description: </p>
 *《停止规则》
 *       流式模式匹配为非贪婪匹配，拥有?、*、+、{m,n}等量词修饰的模式，
 *    在匹配数达到最小值后，应优先搜索其后的模式实例，我们称这些模式实例
 *    为停止标记。搜索停止标记前需要对寄存器压栈，用于搜索停止字符失败时
 *    恢复寄存器状态。
 *       可作为停止标记的有两种，本组模式的实例和下一个模式的实例；
 *    当本模式为当前组的最后一个模式，则拥有两个停止标记，否则仅以下一个
 *    模式的实例作为停止标记。
 *
 *
 *       例如: 对于正则表达式/(5\d+)+6/，组/(5\d+)+/的停止符为模式
 *    /6/的实例，而模式/\d+/的停止符为模式/6/和组/(5\d+)+/的实例
 *
 *
 * @author 肖海星 arksea@gmail.com
 * @version 1.1.6
 */
public class Pattern
{
    public static final int PartMatched_M = 0;
    public static final int Matched_M = 1;
    public static final int NoMatched_M = 2;

    public Pattern(String pattern)
        throws PatternCompiler_E
    {
        Codes codesObj = PatternCache.get(pattern);
        codes = codesObj.codes;
        groupCount = codesObj.groupCount;
        reg = new Register(codes, groupCount);
    }

    /**
     * @directed
     */
    Register reg;
    ////////////////////////////////////////
    byte[] chars = new byte[256];
    int charsLength = 0;
    boolean isEOF=false;
    public byte[] getChars()
    {
        return chars;
    }
    public int getCharsLength()
    {
        return charsLength;
    }

    public boolean isMultiLine() {
        return multiLine;
    }

    Stack searchStopStack = new Stack();//Stack<Register> searchStopStack
    int level = 0; //debug
    boolean multiLine = true;
    boolean searchStopFail;

    public Submatched[][] getMatchedResult()
    {
        int groupCount = 0;
        for (int i=0;i<reg.groups.length;++i)
        {
            if (reg.groups[i].pick)
            {
                ++groupCount;
            }
        }
        Submatched[][] groups;
        groups = new Submatched[groupCount][];
        byte[] chs = new byte[reg.charOffset];
        System.arraycopy(chars, 0, chs, 0, reg.charOffset);
        int index = 1;
        for (int i = 1; i < reg.groups.length; ++i)
        {
            if (reg.groups[i].pick)
            {
                int size = 0;
                Iterator iter=reg.groups[i].items.iterator();
                while(iter.hasNext())
                {
                    Submatched sm=(Submatched)iter.next();
                    if (sm.end == -1 || sm.begin >=reg.charOffset)
                    {
                        break;
                    }
                    ++size;
                }
                groups[index] = new Submatched[size];
                for (int j = 0; j < size; ++j)
                {
                    groups[index][j] = (Submatched)reg.groups[i].items.get(j);
                    groups[index][j].chars = chs;
                }
                ++index;
            }
        }
        groups[0] = new Submatched[1];
        groups[0][0] = new Submatched(chs, 0, reg.charOffset);
        return groups;
    }

    /**
     * @directed
     */
    Code[] codes;
    int groupCount;

    /**
     * 重新开始匹配前重置状态
     */
    public void clear()
    {
        reg.charOffset = 0;
        charsLength = 0;
        reg.reset();
        searchStopStack.clear();
        level = 0; //debug
    }

    /**
     * 不匹配时重置状态
     */
    public void reset()
    {
        //保留未消耗字符,没有已匹配字符的情况除外
        if (reg.charOffset == 0)
        {
            reg.charOffset = 1;
        }
        reg.lastChar=chars[reg.charOffset-1];
        if ( (charsLength - reg.charOffset) >= 1)
        {
            charsLength = charsLength - reg.charOffset;
            for (int i = 0; i < charsLength; ++i)
            {
                chars[i] = chars[reg.charOffset + i];
            }
        }
        else
        {
            charsLength = 0;
        }

        reg.charOffset = 0;
        reg.reset();
        searchStopStack.clear();
        level = 0; //debug
    }

    /**
     * 搜索停止符索引
     * @param next int 下一个字符的索引
     * @return int 停止符的索引
     */
    int getStopOffset(int next)
    {
        if (next < codes.length)
        {
            switch (codes[next].type)
            {
                case Code.OP_OR:
                    next = ( (GroupBegin) codes[ ( (OR) codes[next]).
                            groupOffset])
                        .endOffset;
                    //不break,继续处理GroupEnd
                case Code.GROUP_END:
                    GroupEnd ge = (GroupEnd) codes[next];
                    if (reg.groups[ge.index].items.size() > 0)
                    {
                        ((Submatched)reg.groups[ge.index].
                            items.getLast()).end = reg.charOffset;
                    }
                    int i;
                    for (i = next + 1; i < codes.length; ++i)
                    {
                        if (codes[i].type != Code.GROUP_END)
                        {
                            break;
                        }
                    }
                    if (i == codes.length)
                    {
                        next = i;
                    }
                    break;
                default:
                    break;
            }
        }
        return next;
    }

    public GroupBegin getCurrentGroup()
    {
        return getCurrentGroup(reg.codeOffset);
    }

    public GroupBegin getCurrentGroup(int offset)
    {
        int gbCount = 0;
        find:for (int i = offset; i < codes.length; ++i)
        {
            switch (codes[i].type)
            {
                case Code.GROUP_END:
                    if (gbCount <= 0)
                    {
                        return (GroupBegin) codes[ ( (GroupEnd) codes[i]).
                            beginOffset];
                    }
                    --gbCount;
                    break;
                case Code.GROUP_BEGIN:
                    ++gbCount;
                    break;
                default:
                    break;
            }
        }
        throw new RuntimeException("断言失败");
    }

    /**
     * @param ch byte
     * @return int
     */
    public int check(int c)
    {
        if(c==-1) isEOF=true;
        byte ch=(byte)c;
//        try
//        {
            //缓存待匹配字符,并在需要时自动扩展缓存空间
            if (charsLength >= chars.length)
            {
                byte[] tmp = new byte[chars.length * 2];
                System.arraycopy(chars, 0, tmp, 0, charsLength);
                chars = tmp;
            }
            chars[charsLength] = ch;
            if(ch!=-1)
                ++charsLength;
            //////////////////////////////////////////////////////
            boolean matched = true;
            Repeat repeat; //代码重复数
            int type; //代码类型
            int nxtIndex; //下一个代码的索引
            GroupEnd ge;
            GroupBegin gb;
            Code code;
            boolean hunger;
            do
            {
                code = codes[reg.codeOffset];
                type = code.type;
                repeat = null;
                if (type != Code.OP_OR && type != Code.LINE_HEAD
                    && type != Code.LINE_TAIL)
                {
                    repeat = ( (MatchCode) code).repeat;
                }
                byte lastChar;
                switch (type)
                {
                    case Code.GROUP_BEGIN:
                        gb = (GroupBegin) code;
                        if(isEOF && repeat!=null && reg.groupMatchedCount[gb.index] >= repeat.min)
                        {
                            reg.codeOffset=gb.endOffset+1;
                            break;
                        }
                        if (repeat != null && !searchStopFail)
                        { //搜索停止符的条件(以下各个条件默认为并且关系)
                            //1.有重复修饰符;2.不在searchStopFail状态
                            //3.本组匹配数已达到最小值
                            //4.本组不是正则表达式最后的模式
                            nxtIndex = getStopOffset(gb.endOffset + 1);
                            if ( (reg.groupMatchedCount[gb.index] >= repeat.min)
                                && nxtIndex < codes.length - 1)
                            { //先搜索停止字符
                                if (codes[nxtIndex].type == Code.GROUP_END
                                    && ( (MatchCode) codes[nxtIndex]).repeat != null)
                                {//以父组的下一个实例为停止符
                                    searchStopStack.push( (Register) reg.clone());
                                    reg.codeOffset = ( (GroupEnd) codes[
                                        nxtIndex]).beginOffset;
                                }
                                else
                                {//以下一个模式的实例为停止符
                                    searchStopStack.push( (Register) reg.clone());
                                    reg.codeOffset = nxtIndex;
                                }
                                break;
                            }
                        }
                        searchStopFail = false;
                        //////////////////////////////////////////////////////
                        Submatched sm = new Submatched(chars, reg.charOffset);
                        reg.groups[gb.index].items.add(sm);
                        ++reg.groupMatchedCount[gb.index];
                        ////////////////////////////////////////////////////////
                        ++reg.codeOffset;
                        break;
                    case Code.GROUP_END:
                        ge = (GroupEnd) codes[reg.codeOffset];
                        gb = (GroupBegin) codes[ge.beginOffset];
                        if(isEOF && repeat!=null && reg.groupMatchedCount[gb.index] >= repeat.min)
                        {
                            reg.codeOffset=reg.codeOffset+1;
                            break;
                        }
                        else if (repeat == null)
                        {
                            ((Submatched)reg.groups[ge.index].
                                items.getLast()).end = reg.charOffset;
                            ++reg.codeOffset;
                        }
                        else
                        {
                            if (searchStopFail)
                            { //GroupEnd前的模式在'以后继模式的实例为停止标记'的尝试失败,将进入
                              //以下一个组实例为停止标记的尝试
                                --reg.codeOffset;
                                searchStopStack.push( (Register) reg.clone());
                                reg.matchedCount = 0;
                                ((Submatched)reg.groups[ge.index].
                                    items.getLast()).end = reg.charOffset;
                                reg.codeOffset = ge.beginOffset;
                            }
                            else
                            {
                                int matchedCount =
                                    reg.groupMatchedCount[ge.index];
                                if (matchedCount > 0)
                                {
                                    ((Submatched)reg.groups[ge.index].
                                        items.getLast()).end = reg.charOffset;
                                }
                                if (matchedCount < repeat.max)
                                {
                                    reg.codeOffset = ge.beginOffset;
                                }
                                else
                                {
                                    reg.groupMatchedCount[ge.index]=0;
                                    ++reg.codeOffset;
                                }
                            }
                        }
                        searchStopFail = false;
                        break;
                    case Code.MATCH_CHAR:
                    case Code.MATCH_CHAR_SET:
                        if(isEOF && repeat!=null && reg.matchedCount >= repeat.min)
                        {
                            reg.codeOffset=reg.codeOffset+1;
                            break;
                        }
                        if (repeat != null && !searchStopFail)
                        {
                            searchStopFail = false;
                            nxtIndex = getStopOffset(reg.codeOffset + 1);
                            if (reg.matchedCount >= repeat.min
                                && nxtIndex < codes.length - 1)
                            { //先搜索停止字符
                                if (codes[nxtIndex].type == Code.GROUP_END
                                    && ( (MatchCode) codes[nxtIndex]).repeat != null)
                                {
                                    searchStopStack.push( (Register) reg.clone());
                                    reg.codeOffset = ( (GroupEnd) codes[
                                        nxtIndex]).beginOffset;
                                }
                                else
                                {
                                    searchStopStack.push( (Register) reg.clone());
                                    ++reg.codeOffset;
                                }
                                reg.matchedCount = 0;
                                break;
                            }
                        }
                        if (type == Code.MATCH_CHAR)
                        {
                            MatchChar mc = (MatchChar) codes[reg.codeOffset];
                            matched = matchChar(chars[reg.charOffset],
                                                mc.item,
                                                multiLine);
                        }
                        else
                        {
                            matched = matchCharSet();
                        }
                        if (repeat == null)
                        {
                            if (matched)
                            {
                                ++reg.codeOffset;
                                ++reg.charOffset;
                            }
                            else
                            {
                                reg.matched = false;
                            }
                        }
                        else
                        {
                            if (matched)
                            {
                                ++reg.matchedCount;
                                ++reg.charOffset;
                                if (reg.matchedCount == repeat.max)
                                {
                                    ++reg.codeOffset;
                                    reg.matchedCount = 0;
                                }
                                if(reg.matchedCount > repeat.max)
                                {   throw new RuntimeException("断言失败");
                                }
                            }
                            else if (getStopOffset(reg.codeOffset + 1) >=
                                     codes.length
                                     && reg.matchedCount >= repeat.min
                                     && reg.matchedCount > 0)
                            {
                                reg.matchedCount = 0;
                                ++reg.codeOffset;
                                if(reg.matchedCount > repeat.max)
                                {  throw new RuntimeException("断言失败");
                                }
                            }
                            else
                            {
                                reg.matchedCount = 0;
                                reg.matched = false;
                            }
                        }
                        searchStopFail = false;
                        break;
                    case Code.OP_OR:
                        gb = (GroupBegin) codes[ ( (OR) codes[reg.codeOffset]).
                            groupOffset];
                        if(isEOF && repeat!=null && reg.groupMatchedCount[gb.index] >= repeat.min)
                        {
                            reg.codeOffset=gb.endOffset+1;
                            break;
                        }
                        reg.codeOffset = gb.endOffset;
                        break;
                    case Code.LINE_HEAD:
                        if (reg.charOffset==0)
                        {
                            lastChar = (byte) reg.lastChar;
                        }
                        else
                        {
                            lastChar = chars[reg.charOffset - 1];
                        }
                        if(lastChar == '\r'|| lastChar == '\n')
                        {
                            reg.matched = true;
                            ++reg.codeOffset;
                        }
                        else
                        {
                            reg.matched = false;
                        }
                        break;
                    case Code.LINE_TAIL:
                        if (chars[reg.charOffset] == '\r' ||
                            chars[reg.charOffset] == '\n')
                        {
                            reg.matched = true;
                            ++reg.codeOffset;
                        }
                        else
                        {
                            reg.matched = false;
                        }
                        break;
                    default:
                        throw new RuntimeException("断言失败");
                }
                if (reg.matched)
                {
                    int nxt=getStopOffset(reg.codeOffset);
                    if (reg.codeOffset >= codes.length)
                    { //完全匹配
                        return Matched_M;
                    }
                    //else 部分匹配
                }
                else
                {
                    reg.matched = true;
                    gb = getCurrentGroup();
                    int nextOffset = gb.getNextSubOffset(reg.codeOffset);
                    //////////////////////////////////////////////////////
                    if (nextOffset > 0)
                    {
                        reg.codeOffset = nextOffset;
                        reg.charOffset = ((Submatched)reg.groups[gb.index].items.getLast()).begin;
                    }
                    else if (searchStopStack.size() > 0
                        && ((Register)searchStopStack.peek()).codeOffset!=reg.codeOffset)
                    { //搜索停止字符失败
                        reg = (Register) searchStopStack.pop();
                        searchStopFail = true;
                    }
                    else if(groupMatchedOverMin())
                    {
                        if (getStopOffset(reg.codeOffset + 1) >= codes.length
                             && reg.matchedCount == 0)
                         {//完全匹配
                             return Matched_M;
                         }
                        //部分匹配
                    }
                    else
                    {
                        //不匹配
                        reset();
                        return NoMatched_M;
                    }
                }
                //对于CHAR类型的模式，在碰到数据源“耗尽”时认为是部分匹配，即使用“饥渴”策略
                hunger =
                    reg.charOffset >= charsLength &&
                    (codes[reg.codeOffset].type == Code.MATCH_CHAR ||
                     codes[reg.codeOffset].type == Code.MATCH_CHAR_SET);
            } while ((!hunger || isEOF)
                     && reg.codeOffset < codes.length );
            return PartMatched_M;
//        }
//        finally
//        {
//            reg.lastChar = ch;
//        }
    }
    //模式所在的组，匹配数已超过最小值
    boolean groupMatchedOverMin()
    {
        int offset = reg.codeOffset;
        while (offset < codes.length - 1)
        {
            GroupBegin gb = getCurrentGroup(offset + 1);
            if (gb.repeat != null &&
                reg.groupMatchedCount[gb.index] > gb.repeat.min &&
                reg.groupMatchedCount[gb.index]>1 &&
                getStopOffset(gb.endOffset + 1) == codes.length)
            {
                Submatched sub = (Submatched)reg.groups[gb.index].items.getLast();
                if(sub.end==-1 || sub.begin==-1)
                {
                    reg.groups[gb.index].items.removeLast();
                    --reg.groupMatchedCount[gb.index];
                    reg.codeOffset = gb.endOffset + 1;
                    reg.charOffset = sub.begin;
                }
                return true;
            }
            else
            {
                offset = gb.endOffset;
            }
        }
        return false;
    }

    boolean matchCharSet()
    {
        boolean matched = false;
        MatchCharSet code = (MatchCharSet) codes[reg.codeOffset];
        for (int i = 0; i < code.items.length; ++i)
        {
            if (matchChar(chars[reg.charOffset], code.items[i], true))
            {
                matched = true;
                if (!code.reverse)
                {
                    break;
                }
            }
        }
        if (code.reverse)
        {
            matched = !matched;
        }
        return matched;
    }

    /**
     * 判断指定字符数据与模式是否匹配
     * @param ch byte
     * @param type int
     * @param multiLine boolean
     * @return boolean
     */
    boolean matchChar(int ch, int type, boolean multiLine)
    {
        byte c = (byte)ch;
        if (ch < 0)
        {
            ch += 256;
        }
        if (type < 256)
        {
            if (ch == type)
            {
                return true;
            }
            return false;
        }
        else
        {
            switch (type)
            {
                case CharType.EOF:
                    if(isEOF) return true;
                    else      return false;
                case CharType.ANY:
                    if (!multiLine && ch == '\n')
                    {
                        return false;
                    }
                    return true;
                case CharType.DIGIT:
                    if (ch >= '0' && ch <= '9')
                    {
                        return true;
                    }
                    return false;
                case CharType.NDIGIT:
                    if (ch >= '0' && ch <= '9')
                    {
                        return false;
                    }
                    return true;
                case CharType.WORD:
                    if (ch >= 'a' && ch <= 'z'
                        || ch == '_' ||
                        ch >= 'A' && ch <= 'Z'
                        || ch >= '0' && ch <= '9')
                    {
                        return true;
                    }
                    return false;
                case CharType.NWORD:
                    if (ch >= 'a' && ch <= 'z'
                        || ch == '_' ||
                        ch >= 'A' && ch <= 'Z'
                        || ch >= '0' && ch <= '9')
                    {
                        return false;
                    }
                    return true;
                case CharType.SPACE:
                    if (ch == ' ' || ch == '\t'
                        || multiLine &&
                        (ch == '\r' || ch == '\n'))
                    {
                        return true;
                    }
                    return false;
                case CharType.NSPACE:
                    if (ch == ' ' || ch == '\t'
                        || multiLine &&
                        (ch == '\r' || ch == '\n'))
                    {
                        return false;
                    }
                    return true;
                default:
                    throw new RuntimeException("断言失败");
            }
        }
    }

    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
    }
}
