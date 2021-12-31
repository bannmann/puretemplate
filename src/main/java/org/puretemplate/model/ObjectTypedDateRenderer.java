package org.puretemplate.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apiguardian.api.API;

/**
 * A renderer for {@link Date} and {@link Calendar}. <br>
 * <br>
 * The format string in the template can be
 * <ul>
 *     <li>a {@linkplain java.text.SimpleDateFormat custom pattern}</li>
 *     <li>one of the standard {@linkplain java.time.format.FormatStyle formatting styles}
 *     <ul>
 *         <li>{@code "short"}, typically numeric. Examples: '12.13.52' or '3:30pm'</li>
 *         <li>{@code "medium"}, with some detail. Example: 'Jan 12, 1952'</li>
 *         <li>{@code "long"}, with lots of detail. Example: 'January 12, 1952'</li>
 *         <li>{@code "full"}, with the most detail. Examples: 'Tuesday, April 12, 1952 AD' or '3:30:42pm PST'</li>
 *     </ul>
 *     </li>
 *     <li>
 *         one of the standard formatting styles (see above), but prefixed by {@code "date:"} or {@code "time:"} to show
 *         only those components of the time object
 *     </li>
 *     <li>left out to use the default ({@code "short"})</li>
 * </ul>
 *
 * @see CalendarRenderer
 * @see DateRenderer
 * @see DateTimeRenderer
 * @deprecated Use {@link DateRenderer} or {@link CalendarRenderer} instead. This class stems from StringTemplate, where
 * {@link Object} was used as the {@link AttributeRenderer} type parameter to avoid having two separate classes. To
 * avoid <a href="https://github.com/antlr/stringtemplate4/issues/288">pitfalls</a>, {@code ObjectTypedDateRenderer}
 * will be removed in a future release of PureTemplate.
 */
@Deprecated(forRemoval = true)
@API(status = API.Status.DEPRECATED)
public class ObjectTypedDateRenderer implements AttributeRenderer<Object>
{
    /**
     * Renders the given {@code Date} or {@code Calendar}.
     *
     * @param value the {@code Date} or {@code Calendar} object to render
     * @param formatString a custom pattern or a predefined style; {@code null} activates the default. For details, see
     * the {@linkplain ObjectTypedDateRenderer class description}.
     * @param locale the active locale, never {@code null}
     *
     * @throws ClassCastException if the given object is neither {@link Date} nor {@link Calendar}.
     */
    @Override
    public String render(Object value, String formatString, Locale locale)
    {
        Date date = getDate(value);
        return Dates.format(date, formatString, locale);
    }

    private Date getDate(Object value)
    {
        if (value instanceof Calendar)
        {
            return ((Calendar) value).getTime();
        }
        else
        {
            return (Date) value;
        }
    }
}
