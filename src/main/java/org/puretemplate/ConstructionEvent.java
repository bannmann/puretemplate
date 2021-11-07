package org.puretemplate;

/**
 * An event that happens when building ST trees, adding attributes etc...
 */
class ConstructionEvent implements Event
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
