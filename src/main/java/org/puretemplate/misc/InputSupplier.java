package org.puretemplate.misc;

import java.io.IOException;

/**
 * Supplies the input source to use for loading operations. This functional interface is intended to allow lazy
 * evaluation when specifying how to load templates or groups.
 *
 * @param <T> the type of input source supplied by this supplier
 *
 * @since 1.8
 */
@FunctionalInterface
public interface InputSupplier<T>
{
    T get() throws IOException;
}
