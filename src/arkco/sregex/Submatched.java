package arkco.sregex;

/**
 * <p>Title: “—∆•≈‰ƒ⁄»›ª∫¥Ê</p>
 *
 * @author –§∫£–« arksea@gmail.com
 * @version 1.1.6
 */
public class Submatched implements Cloneable,java.io.Serializable
{
    public Submatched(byte[] chars)
    {
        this.chars=chars;
    }
    public Submatched(byte[] chars,int begin,int end)
    {
        this.begin=begin;
        this.end=end;
        this.chars=chars;
    }
    public Submatched(byte[] chars,int begin)
    {
        this.begin=begin;
        //this.end=begin;
        this.chars=chars;
    }
    /**
     * @return byte[]
     */
    public byte[] getBytes()
    {
        if(begin==0 && end==chars.length)
        {
            return chars;
        }
        else
        {
            byte[] buf=new byte[end-begin];
            System.arraycopy(chars,begin,buf,0,end-begin);
            return buf;
        }
    }
    /**
     * @return String
     */
    public String toString()
    {
        if(matched==null)
        {
            synchronized(this)
            {
                if(matched==null)
                {
                    matched = new String(chars, begin, end - begin);
                }
            }
        }
        return matched;
    }
    int begin=-1;
    int end=-1;
    String matched=null;
    byte[] chars;
    public Object clone()
    {
        Submatched obj=null;
        try
        {
            obj = (Submatched)super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new RuntimeException("∂œ—‘ ß∞‹");
        }
        return obj;
    }
}
