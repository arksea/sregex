package arkco.sregex;

import java.util.LinkedList;

/**
 * �ڵ����
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
    //����ֵ
    final static int
        GROUP_BEGIN = 0,
        GROUP_END = 1,
        OP_OR = 2,
        MATCH_CHAR_SET = 3,
        MATCH_CHAR = 4,
        LINE_HEAD = 5,
        LINE_TAIL = 6;
    int type; //����
}

class OR extends Code
{
    public OR()
    {
        this.type = OP_OR;
    }
    /**
     * ����group����
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
     * ���ݵ��ظ���
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
     * ��֮��Ե�GroupEnd�Ĵ�������
     */
    int endOffset = 0;
    /**
     * ��group�е�����group[index]
     */
    int index=-1;
    /**
     * �Ƿ���ȡ,pick=false��ʾ(?:...)
     */
    boolean pick=true;
    /**
     * Group����OR�ָ����ӱ��ʽ������
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
    boolean reverse; //�Ƿ��Ƿ���
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

