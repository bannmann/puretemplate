package org.puretemplate.model;

import java.util.Locale;

/**
 * This interface describes an object that knows how to format or otherwise render an object appropriately. There is one
 * renderer registered per group for a given Java type.
 *
 * <p>
 * If the format string passed to the renderer is not recognized then simply call {@link Object#toString}.</p>
 *
 * <p>
 * {@code formatString} can be {@code null} but {@code locale} will at least be {@link Locale#getDefault}.</p>
 *
 * @param <T> the type of values this renderer can handle.
 */
public interface AttributeRenderer<T>
{
    String toString(T value, String formatString, Locale locale);
}
