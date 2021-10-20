package org.puretemplate;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides shared configuration to templates and a way for them to resolve references to other templates.
 */
@Immutable
@ThreadSafe
public interface Group
{
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
