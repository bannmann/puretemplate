package org.puretemplate.exception;

import lombok.Getter;

import org.apiguardian.api.API;

/**
 * For {@code <a.b>}, object {@code a} does not have a property {@code b}.
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
