package org.puretemplate;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.apiguardian.api.API;

/**
 * Provides shared configuration to templates and a way for them to resolve references to other templates.
 */
@API(status = API.Status.STABLE)
@Immutable
@ThreadSafe
public interface Group
{
    /**
     * Gets the name of this template group.
     *
     * @return the name
     */
    String getName();

    /**
     * Retrieves the template with the given name.
     *
     * @param name absolute, fully-qualified template name like {@code /a/b}.
     *
     * @return the template
     *
     * @throws IllegalArgumentException if {@code name} is invalid
     * @throws NullPointerException if name is {@code null}
     */
    Template getTemplate(String name);
}
