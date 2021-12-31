package org.puretemplate.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.apiguardian.api.API;

import com.google.common.collect.ImmutableMap;

/**
 * A renderer for {@link LocalDateTime}, {@link ZonedDateTime} and other {@code java.time} classes. <br>
 * <br>
 * The format string in the template can be
 * <ul>
 *     <li>a {@linkplain java.text.SimpleDateFormat custom pattern}</li>
 *     <li>one of the standard {@linkplain FormatStyle formatting styles}
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
 *     <li>left out to use the default:
 *     <ul>
 *         <li>{@code "date:short"} for {@link LocalDate}</li>
 *         <li>{@code "time:short"} for {@link LocalTime}</li>
 *         <li>{@code "short"} otherwise</li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * @see CalendarRenderer
 * @see DateRenderer
 */
@API(status = API.Status.EXPERIMENTAL)
public class DateTimeRenderer<T extends TemporalAccessor> implements AttributeRenderer<T>
{
    private static final Map<String, Supplier<DateTimeFormatter>> FORMATTERS = createFormatterMap();

    private static Map<String, Supplier<DateTimeFormatter>> createFormatterMap()
    {
        ImmutableMap.Builder<String, Supplier<DateTimeFormatter>> result = ImmutableMap.builder();
        for (FormatStyle style : FormatStyle.values())
        {
            String key = style.name()
                .toLowerCase(Locale.ROOT);
            result.put(key, () -> DateTimeFormatter.ofLocalizedDateTime(style));
            result.put("date:" + key, () -> DateTimeFormatter.ofLocalizedDate(style));
            result.put("time:" + key, () -> DateTimeFormatter.ofLocalizedTime(style));
        }
        return result.build();
    }

    /**
     * Renders the given time object.
     *
     * @param value the time object to render
     * @param formatString a custom pattern or a predefined style; {@code null} activates the default. For details, see
     * the {@linkplain DateTimeRenderer class description}.
     * @param locale the active locale, never {@code null}
     */
    @Override
    public String render(T value, String formatString, Locale locale)
    {
        return getFormatter(value, formatString).withLocale(locale)
            .format(value);
    }

    private DateTimeFormatter getFormatter(T value, String formatString)
    {
        if (formatString == null)
        {
            formatString = getDefaultFormat(value);
        }

        Supplier<DateTimeFormatter> supplier = FORMATTERS.get(formatString);
        if (supplier == null)
        {
            return DateTimeFormatter.ofPattern(formatString);
        }
        else
        {
            return supplier.get();
        }
    }

    private String getDefaultFormat(T value)
    {
        if (value instanceof LocalDate)
        {
            return "date:short";
        }

        if (value instanceof LocalTime)
        {
            return "time:short";
        }

        return "short";
    }
}
