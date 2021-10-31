package org.puretemplate.error;

import java.io.PrintWriter;
import java.io.StringWriter;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apiguardian.api.API;
import org.puretemplate.misc.Location;

/**
 * Upon error, ST creates a {@link Message} or subclass instance and notifies the listener.  This root class is used for
 * IO and internal errors.
 */
@API(status = API.Status.MAINTAINED)
@Getter
@AllArgsConstructor
public class Message
{
    protected final ErrorType error;
    protected final Location location;
    protected final Throwable cause;
    protected final Object arg;
    protected final Object arg2;
    protected final Object arg3;

    public Message(ErrorType error, Location location, Throwable cause)
    {
        this(error, location, cause, null, null, null);
    }

    public Message(ErrorType error, Location location, Throwable cause, Object arg)
    {
        this(error, location, cause, arg, null, null);
    }

    public Message(ErrorType error, Location location, Throwable cause, Object arg, Object arg2)
    {
        this(error, location, cause, arg, arg2, null);
    }

    @Override
    public String toString()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String msg = String.format(error.getMessage(), arg, arg2, arg3);
        pw.print(msg);
        if (cause != null)
        {
            pw.print("\nCaused by: ");
            cause.printStackTrace(pw);
        }
        return sw.toString();
    }
}
