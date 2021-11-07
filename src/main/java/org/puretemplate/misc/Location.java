package org.puretemplate.misc;

import java.io.Serializable;
import java.util.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

/**
 * Provides details about the part of the template source code that caused an error.
 */
@Value
public class Location implements Serializable
{
    Location parent;

    @NonNull
    @Getter
    String templateName;

    Coordinates coordinates;

    String sourceText;

    String reference;

    public Optional<Location> getParent()
    {
        return Optional.ofNullable(parent);
    }

    public Optional<Coordinates> getCoordinates()
    {
        return Optional.ofNullable(coordinates);
    }

    /**
     * If an instance of <i>x</i> is enclosed in a <i>y</i> which is in a <i>z</i>, return a {@code String} of these
     * instance names in order from topmost to lowest; here that would be {@code [z y x]}.
     */
    public String getCallHierarchy()
    {
        StringBuilder result = new StringBuilder();
        appendNames(result);
        return result.toString();
    }

    private void appendNames(StringBuilder result)
    {
        if (parent != null)
        {
            parent.appendNames(result);
            result.append(' ');
        }
        result.append(templateName);
    }
}
