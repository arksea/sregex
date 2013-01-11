package arkco.sregex;

import java.io.PrintWriter;
import java.io.PrintStream;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Revision: 1.1.5.1 $
 */
public class RegexException extends Exception
{
    public RegexException()
    {}

    public RegexException(String msg)
    {
        super(msg);
    }
    public RegexException(String msg,Throwable ex)
    {
        super(msg);
        cause=ex;
    }
    public RegexException(Throwable ex)
    {
        this.cause=ex;
    }
    Throwable cause;
    public void printStackTrace()
    {
        synchronized(System.err)
        {
            super.printStackTrace();
            if(cause!=null)
            {
                System.err.print("Caused by: ");
                cause.printStackTrace();
            }
        }
    }
    public void printStackTrace(PrintStream printstream)
    {
        synchronized(printstream)
        {
            super.printStackTrace(printstream);
            if(cause!=null)
            {
                printstream.print("Caused by: ");
                cause.printStackTrace(printstream);
            }
        }
    }
    public void printStackTrace(PrintWriter printwriter)
    {
        synchronized(printwriter)
        {
            super.printStackTrace(printwriter);
            if(cause!=null)
            {
                printwriter.write("Caused by: ");
                cause.printStackTrace(printwriter);
            }
        }
    }
}
