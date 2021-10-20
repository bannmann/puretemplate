package org.puretemplate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.puretemplate.misc.Location;

/**
 * {@code <name>} where {@code name} is not found up the dynamic scoping chain.
 */
@Getter
@RequiredArgsConstructor
public final class NoSuchAttributeException extends TemplateException
{
    private final String name;
    private final Location location;

    @Override
    public String getMessage()
    {
        if (location != null)
        {
            return "from template " + location.getTemplateName() + " no attribute " + name + " is visible";
        }
        else
        {
            return "no such attribute: " + name;
        }
    }
}
