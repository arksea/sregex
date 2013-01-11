package arkco.sregex;

import java.util.LinkedList;
import java.util.Iterator;

class Group implements Cloneable
{
    LinkedList items = new LinkedList();//LinkedList<Submatched> items
    boolean pick = true;
    public Object clone()
    {
        Group dgr = null;
        try
        {
            dgr = (Group)super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new RuntimeException("����ʧ��");
        }
        dgr.items=new LinkedList();
        Iterator iter=this.items.iterator();
        while(iter.hasNext())
        {
            Submatched sm =(Submatched)iter.next();
            dgr.items.add( (Submatched) sm.clone());
        }
        return dgr;
    }
}

class Register implements Cloneable
{
    public Register(Code[] codes,int groupCount)
    {
        groups = new Group[groupCount];
        groupMatchedCount = new int[groupCount];
        int index=0;
        for(int i=0;i<codes.length;++i)
        {
            if(codes[i].type==Code.GROUP_BEGIN)
            {
                groups[index]=new Group();
                groups[index].pick=((GroupBegin)codes[i]).pick;
                ++index;
            }
        }
    }

    /**
     * @directed
     */
    Group[] groups;
    int codeOffset=0; //ָ���ַ�Ĵ���
    int charOffset;
    int lastChar='\n';
    int matchedCount=0; //��ָ����ƥ��Ĵ���
    int[] groupMatchedCount; //����ƥ��Ĵ���
    boolean matched=true;

    public void reset()
    {
        codeOffset = 0;
        matched = true;
        matchedCount = 0;
        for (int i=0;i<groupMatchedCount.length;++i)
        {
            groupMatchedCount[i] = 0;
        }

        for(int i=0;i<groups.length;++i)
        {
            groups[i].items.clear();
        }
    }

    public Object clone()
    {
        Register reg = null;
        try
        {
            reg = (Register)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException("����ʧ��");
        }
        reg.groups = new Group[this.groups.length];
        for (int i=0;i<reg.groups.length;++i)
        {
            reg.groups[i]=(Group)this.groups[i].clone();
        }
        reg.groupMatchedCount = new int[this.groupMatchedCount.length];
        for (int i=0;i<reg.groupMatchedCount.length;++i)
        {
            reg.groupMatchedCount[i]=
                this.groupMatchedCount[i];
        }
        return reg;
    }
}
