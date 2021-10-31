package org.puretemplate.exception;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
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
