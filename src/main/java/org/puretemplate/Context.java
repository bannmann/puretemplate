package org.puretemplate;

import java.util.Locale;

import javax.annotation.concurrent.NotThreadSafe;

import org.apiguardian.api.API;
import org.puretemplate.diagnostics.ContextDiagnostics;
import org.puretemplate.error.ErrorListener;

/**
 * A context for which to render the {@link Template} that created it. <br>
 * <br>
 * A context is intended to be used from one thread only. Unlike with {@link Template}, there are no plans to make
 * contexts thread-safe.
 */
@API(status = API.Status.STABLE)
@NotThreadSafe
public interface Context
{
    /**
     * Inject an attribute (name/value pair). If there is already an attribute with that name, creates a new list with
     * both the previous and the new attribute as elements. If the previous and/or new value is a list, the resulting
     * value is a new list joining all elements. The same applies for other collections and arrays.<br>
     * <br>
     * Note that while you can nest templates by passing a {@link Context} as {@code value}, you cannot add the {@link
     * Template} this way. Allowing this would likely hide logic errors in the calling code.
     *
     * @param name must be non-null, cannot contain '.'
     * @param value the value
     *
     * @throws IllegalArgumentException if {@code name} is invalid or {@code value} is/contains a {@link Template} (as
     * opposed to a {@link Context})
     * @throws NullPointerException if {@code name} is {@code null}
     */
    Context add(String name, Object value);

    /**
     * Remove an attribute value entirely (can't remove attribute definitions).
     */
    Context remove(String name);

    /**
     * Sets the locale to use for formatting operations. If no locale is set for the template group or the context,
     * PureTemplate defaults to {@link Locale#ROOT}.
     *
     * @param locale the locale to use, must not be {@code null}.
     */
    Context setLocale(Locale locale);

    /**
     * Sets the locale to use for formatting operations. If no locale is set for the template group or the context,
     * PureTemplate defaults to {@link Locale#ROOT}.<br>
     * <br>
     * Invoking this is equivalent to {@code setLocale(Locale.getDefault(Locale.Category.FORMAT))}.
     */
    Context useSystemDefaultLocale();

    /**
     * Sets the error listener to use during rendering. Defaults to the template/group settings.
     */
    Context setErrorListener(ErrorListener errorListener);

    /**
     * Renders the template using the values and settings in this Context.
     *
     * @return the initial state of the fluent API. See <a href="package-summary.html#fluent-api-usage-notes">Usage
     * notes for fluent APIs in PureTemplate</a> for details.
     */
    Renderer render();

    /**
     * Retrieves low-level diagnostics data.
     */
    @API(status = API.Status.EXPERIMENTAL)
    ContextDiagnostics diagnostics();
}
