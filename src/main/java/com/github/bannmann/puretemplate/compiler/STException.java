package com.github.bannmann.puretemplate.compiler;

public class STException extends RuntimeException
{
    // no checking damnit!
    public STException()
    {
    }

    public STException(String msg, Exception cause)
    {
        super(msg, cause);
    }
}
