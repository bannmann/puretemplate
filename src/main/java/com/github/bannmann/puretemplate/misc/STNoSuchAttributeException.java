package com.github.bannmann.puretemplate.misc;

import lombok.RequiredArgsConstructor;

import com.github.bannmann.puretemplate.InstanceScope;
import com.github.bannmann.puretemplate.compiler.STException;

/**
 * {@code <name>} where {@code name} is not found up the dynamic scoping chain.
 */
@RequiredArgsConstructor
public class STNoSuchAttributeException extends STException
{
    public final String name;
    public final InstanceScope scope;

    @Override
    public String getMessage()
    {
        if (scope != null)
        {
            return "from template " + scope.st.getName() + " no attribute " + name + " is visible";
        }
        else
        {
            return "no such attribute: " + name;
        }
    }
}
