package org.puretemplate.exception;

import org.apiguardian.api.API;

/**
 * Thrown when more than one value is mapped to a certain type.
 */
@API(status = API.Status.STABLE)
public final class AmbiguousMatchException extends TemplateException
{
    public AmbiguousMatchException(String message)
    {
        super(message);
    }
}
