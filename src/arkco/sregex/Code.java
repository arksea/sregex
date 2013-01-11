package arkco.sregex;

import java.util.LinkedList;

/**
 * 节点代码
 */
class Codes
{
    /**
     * @directed
     */
    Code[] codes;
    int groupCount;
}

abstract class Code
{
    //类型值
    final static int
        GROUP_BEGIN = 0,
        GROUP_END = 1,
        OP_OR = 2,
        MATCH_CHAR_SET = 3,
        MATCH_CHAR = 4,
        LINE_HEAD = 5,
        LINE_TAIL = 6;
    int type; //类型
}

class OR extends Code
{
    public OR()
    {
        this.type = OP_OR;
    }
    /**
     * 所在group索引
     */
    int groupOffset=0;
}
class LineHead extends Code
{
    public LineHead()
    {
        this.type = LINE_HEAD;
    }
}
class LineTail extends Code
{
    public LineTail()
    {
        this.type=LINE_TAIL;
    }
}
abstract class MatchCode extends Code
{
    /**
     * 数据的重复数
     * @directed
     */
    Repeat repeat = null;
}

class GroupBegin extends MatchCode
{
    public GroupBegin()
    {
        this.type = GROUP_BEGIN;
    }
    /**
     * 与之配对的GroupEnd的代码索引
     */
    int endOffset = 0;
    /**
     * 在group中的索引group[index]
     */
    int index=-1;
    /**
     * 是否提取,pick=false表示(?:...)
     */
    boolean pick=true;
    /**
     * Group中用OR分隔的子表达式的索引
     */
    int[] sub;
    public int getNextSubOffset(int curOffset)
    {
        for(int i=0;i<sub.length;++i)
        {
            if(sub[i]>curOffset)
            {
                return sub[i];
            }
        }
        return -1;
    }
}
class GroupEnd extends MatchCode
{
    public GroupEnd()
    {
        this.type = GROUP_END;
    }
    int beginOffset;
    int index=-1;
    boolean pick=true;
}
class MatchCharSet extends MatchCode
{
    int[] items;
    boolean reverse; //是否是反集
    public MatchCharSet(boolean _reverse, int[] _items)
    {
        type = MATCH_CHAR_SET;
        this.reverse = _reverse;
        this.items = _items;
    }
}

class MatchChar extends MatchCode
{
    int item;
    public MatchChar(int _item)
    {
        type = MATCH_CHAR;
        this.item = _item;
    }
}

