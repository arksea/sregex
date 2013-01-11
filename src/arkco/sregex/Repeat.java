package arkco.sregex;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author ark arkqq@hotmail.com
 * @version $Revision: 1.1.5.1 $
 */
class Repeat implements Cloneable
{
    //+ = {1,}
    //* = {0,}
    //? = {0,1}
    int min,max;
    public Repeat(int min,int max)
    {
        this.min=min;
        this.max=max;
    }
    public Object clone()
    {
        Repeat dec = null;
        try
        {
            dec = (Repeat)super.clone();
        }
        catch (CloneNotSupportedException e)
        { throw new RuntimeException("∂œ—‘ ß∞‹");}
        return dec;
    }
}
