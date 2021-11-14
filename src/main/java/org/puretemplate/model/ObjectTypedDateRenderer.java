package org.puretemplate.model;

import static java.util.Map.entry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apiguardian.api.API;

/**
 * A renderer for {@link Date} and {@link Calendar} objects. It understands a variety of format names as shown in {@link
 * #FORMATS} field. By default it assumes {@code "short"} format. A prefix of {@code "date:"} or {@code "time:"} shows
 * only those components of the time object.<br>
 * <br>
 * <b>API status:</b> {@link API.Status#MAINTAINED} because PureTemplate will most likely fix
 * <a href="https://github.com/antlr/stringtemplate4/issues/288">antlr/stringtemplate4#288</a> by replacing this with
 * two distinct classes for {@link Date} and {@link Calendar}.
 */
// using <Object> because this can handle Date and Calendar objects, which don't have a common supertype.
@API(status = API.Status.MAINTAINED)
public class ObjectTypedDateRenderer implements AttributeRenderer<Object>
{
    private static final Map<String, Integer> FORMATS = Map.ofEntries(entry("short", DateFormat.SHORT),
        entry("medium", DateFormat.MEDIUM),
        entry("long", DateFormat.LONG),
        entry("full", DateFormat.FULL),
        entry("date:short", DateFormat.SHORT),
        entry("date:medium", DateFormat.MEDIUM),
        entry("date:long", DateFormat.LONG),
        entry("date:full", DateFormat.FULL),
        entry("time:short", DateFormat.SHORT),
        entry("time:medium", DateFormat.MEDIUM),
        entry("time:long", DateFormat.LONG),
        entry("time:full", DateFormat.FULL));

    @Override
    public String render(Object value, String formatString, Locale locale)
    {
        Date date = getDate(value);
        DateFormat dateFormat = getDateFormat(formatString, locale);
        return dateFormat.format(date);
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

    private DateFormat getDateFormat(String formatString, Locale locale)
    {
        if (formatString == null)
        {
            formatString = "short";
        }
        Integer style = FORMATS.get(formatString);

        if (style == null)
        {
            return new SimpleDateFormat(formatString, locale);
        }

        if (formatString.startsWith("date:"))
        {
            return DateFormat.getDateInstance(style, locale);
        }

        if (formatString.startsWith("time:"))
        {
            return DateFormat.getTimeInstance(style, locale);
        }

        return DateFormat.getDateTimeInstance(style, style, locale);
    }
}
