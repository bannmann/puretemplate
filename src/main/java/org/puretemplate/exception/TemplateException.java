package org.puretemplate.exception;

import org.apiguardian.api.API;

/**
 * Base class of PureTemplate exceptions.
 */
@API(status = API.Status.STABLE)
public abstract class TemplateException extends RuntimeException
{
    TemplateException()
    {
    }

    TemplateException(String message)
    {
        super(message);
    }

    TemplateException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
