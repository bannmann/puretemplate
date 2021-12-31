package org.puretemplate.model;

import java.util.Date;
import java.util.Locale;

import org.apiguardian.api.API;

/**
 * A renderer for {@link Date}. <br>
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
 * @see DateTimeRenderer
 */
@API(status = API.Status.EXPERIMENTAL)
public class DateRenderer implements AttributeRenderer<Date>
{
    /**
     * Renders the given {@code Date}.
     *
     * @param date the date to render
     * @param formatString a custom pattern or a predefined style; {@code null} activates the default. For details, see
     * the {@linkplain DateRenderer class description}.
     * @param locale the active locale, never {@code null}
     */
    @Override
    public String render(Date date, String formatString, Locale locale)
    {
        return Dates.format(date, formatString, locale);
    }
}
