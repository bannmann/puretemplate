package org.puretemplate.exception;

public class TemplateException extends RuntimeException
{
    public TemplateException()
    {
    }

    public TemplateException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
