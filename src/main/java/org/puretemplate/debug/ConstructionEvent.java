package org.puretemplate.debug;

import org.apiguardian.api.API;

/**
 * An event that happens when building ST trees, adding attributes etc...
 */
@API(status = API.Status.EXPERIMENTAL)
public class ConstructionEvent
{
    public Throwable stack;

    public ConstructionEvent()
    {
        stack = new Throwable();
    }

    public String getFileName()
    {
        return getSTEntryPoint().getFileName();
    }

    public int getLine()
    {
        return getSTEntryPoint().getLineNumber();
    }

    public StackTraceElement getSTEntryPoint()
    {
        StackTraceElement[] trace = stack.getStackTrace();
        for (StackTraceElement e : trace)
        {
            String name = e.toString();
            if (!name.startsWith("org.puretemplate"))
            {
                return e;
            }
        }
        return trace[0];
    }
}
