package org.puretemplate.exception;

import lombok.Getter;

import org.apiguardian.api.API;

/**
 * Thrown when an object does not have the property requested by the template.
 */
@API(status = API.Status.STABLE)
@Getter
public final class NoSuchPropertyException extends TemplateException
{
    private final transient Object o;
    private final String propertyName;

    public NoSuchPropertyException(Exception e, Object o, String propertyName)
    {
        super(null, e);
        this.o = o;
        this.propertyName = propertyName;
    }

    @Override
    public String getMessage()
    {
        if (o != null)
        {
            return "object " + o.getClass() + " has no " + propertyName + " property";
        }
        else
        {
            return "no such property: " + propertyName;
        }
    }
}
