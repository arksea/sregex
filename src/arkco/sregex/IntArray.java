package arkco.sregex;

import java.util.EmptyStackException;
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
public class IntArray
{
    public IntArray()
    {
        mSize = 20;
        mBuf = new int[mSize];
    }
    public IntArray(int initSize)
    {
        mSize = initSize;
        mBuf = new int[mSize];
    }
    private int mSize;
    private int[] mBuf;
    private int mLength = 0;

    public void put(int b)
    {
        if (mLength >= mSize)
        {
            mSize *= 2;
            int[] tmp = new int[mSize];
            System.arraycopy(mBuf, 0, tmp, 0, mLength);
            mBuf = tmp;
        }
        mBuf[mLength++] = b;
    }
    //移除最后一个元素
    public int pop() throws EmptyStackException
    {
        if(mLength>0)
        {
            return mBuf[--mLength];
        }
        throw new ArrayIndexOutOfBoundsException();
    }
    public int get(int index) throws ArrayIndexOutOfBoundsException
    {
        if(index>=mLength)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        return mBuf[index];
    }
    public void set(int index,int b) throws ArrayIndexOutOfBoundsException
    {
        if(index>=mLength)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        mBuf[index]=b;
    }

    public void clear()
    {
       mLength=0;
    }

    public int[] getArray()
    {
        int[] tmp = new int[mLength];
        System.arraycopy(mBuf, 0, tmp, 0, mLength);
        return tmp;
    }


    public int length()
    {
        return mLength;
    }

    public int size()
    {
        return mSize;
    }

    public Object clone()
    {
        IntArray tmp = new IntArray();
        tmp.mLength = this.mLength;
        tmp.mSize = this.mSize;
        tmp.mBuf = new int[this.mBuf.length];
        System.arraycopy(this.mBuf,0,tmp.mBuf,0,this.mBuf.length);
        return tmp;
    }
}
