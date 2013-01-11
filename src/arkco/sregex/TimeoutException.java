package arkco.sregex;

import java.io.IOException;

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
public class TimeoutException extends IOException
{
    public TimeoutException()
    {
        super();
    }

    public TimeoutException(String message)
    {
        super(message);
    }
}
