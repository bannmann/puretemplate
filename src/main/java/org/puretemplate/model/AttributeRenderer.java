package org.puretemplate.model;

import java.util.Locale;

import org.apiguardian.api.API;

/**
 * Renders or formats attributes appropriately. There is one renderer registered per group for a given Java type.
 *
 * @param <T> the type of values this renderer can handle.
 */
@API(status = API.Status.STABLE)
public interface AttributeRenderer<T>
{
    /**
     * Renders the given non-null value as a {@link String}. <br>
     * <br>
     * Note: {@code null} is never passed to attribute renderers. However, if a template specifies a replacement string
     * for {@code null}, that value <i>is</i> passed to a renderer registered for {@code String}.
     *
     * @param value the object to render, never {@code null}
     * @param formatString format string or {@code null} if unspecified
     * @param locale the active locale, never {@code null}
     */
    String render(T value, String formatString, Locale locale);
}
