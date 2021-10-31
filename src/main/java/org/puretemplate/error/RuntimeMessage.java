package org.puretemplate.error;

import org.apiguardian.api.API;
import org.puretemplate.misc.Location;

/**
 * Used to track errors that occur in the ST interpreter.
 */
@API(status = API.Status.MAINTAINED)
public final class RuntimeMessage extends Message
{
    public RuntimeMessage(ErrorType error, Location location)
    {
        this(error, location, null);
    }

    public RuntimeMessage(ErrorType error, Location location, Object arg)
    {
        this(error, location, null, arg, null);
    }

    public RuntimeMessage(ErrorType error, Location location, Throwable e, Object arg)
    {
        this(error, location, e, arg, null);
    }

    public RuntimeMessage(ErrorType error, Location location, Throwable e, Object arg, Object arg2)
    {
        this(error, location, e, arg, arg2, null);
    }

    public RuntimeMessage(ErrorType error, Location location, Throwable e, Object arg, Object arg2, Object arg3)
    {
        super(error, location, e, arg, arg2, arg3);
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        if (location != null)
        {
            buf.append("context [")
                .append(location.getCallHierarchy())
                .append("]");

            location.getCoordinates()
                .ifPresent(coordinate -> buf.append(" ")
                    .append(coordinate));
        }
        buf.append(" ")
            .append(super.toString());
        return buf.toString();
    }
}
