package org.puretemplate;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Represents a template which is ready for being rendered. <br>
 * <br>
 * Note: In some future PureTemplate release, any Template instance will be fully loaded, immutable and thread-safe. If
 * all goes well, that is.
 */
@NotThreadSafe
public interface Template
{
    /**
     * Creates a new context for rendering this template.
     */
    Context createContext();
}
