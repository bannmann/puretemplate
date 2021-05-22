package com.github.bannmann.puretemplate.misc;

import com.github.bannmann.puretemplate.InstanceScope;
import com.github.bannmann.puretemplate.compiler.STException;

/**
 * {@code <name>} where {@code name} is not found up the dynamic scoping chain.
 */
public class STNoSuchAttributeException extends STException
{
    public InstanceScope scope;
    public String name;

    public STNoSuchAttributeException(String name, InstanceScope scope)
    {
        this.name = name;
        this.scope = scope;
    }

    @Override
    public String getMessage()
    {
        return "from template " + scope.st.getName() + " no attribute " + name + " is visible";
    }
}
