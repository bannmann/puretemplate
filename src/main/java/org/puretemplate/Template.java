package org.puretemplate;

import javax.annotation.concurrent.NotThreadSafe;

import org.apiguardian.api.API;

/**
 * Represents a template which is ready for being rendered. <br>
 * <br>
 * Note: In some future PureTemplate release, any Template instance will be fully loaded, immutable and thread-safe. If
 * all goes well, that is.
 */
@API(status = API.Status.STABLE)
@NotThreadSafe
public interface Template
{
    /**
     * Creates a new context for rendering this template.
     */
    Context createContext();
}
