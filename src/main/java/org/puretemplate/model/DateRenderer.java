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
 * #formatToInt} field. By default it assumes {@code "short"} format. A prefix of {@code "date:"} or {@code "time:"}
 * shows only those components of the time object.<br>
 * <br>
 * <b>API status:</b> {@link API.Status#MAINTAINED} because PureTemplate will most likely fix
 * <a href="https://github.com/antlr/stringtemplate4/issues/288">antlr/stringtemplate4#288</a> by replacing this with
 * two distinct classes for {@link Date} and {@link Calendar}.
 */
// using <Object> because this can handle Date and Calendar objects, which don't have a common supertype.
@API(status = API.Status.MAINTAINED)
public class DateRenderer implements AttributeRenderer<Object>
{
    public static final Map<String, Integer> formatToInt = Map.ofEntries(entry("short", DateFormat.SHORT),
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
    public String toString(Object value, String formatString, Locale locale)
    {
        Date d;
        if (formatString == null)
        {
            formatString = "short";
        }
        if (value instanceof Calendar)
        {
            d = ((Calendar) value).getTime();
        }
        else
        {
            d = (Date) value;
        }
        Integer styleI = formatToInt.get(formatString);
        DateFormat f;
        if (styleI == null)
        {
            f = new SimpleDateFormat(formatString, locale);
        }
        else
        {
            int style = styleI.intValue();
            if (formatString.startsWith("date:"))
            {
                f = DateFormat.getDateInstance(style, locale);
            }
            else if (formatString.startsWith("time:"))
            {
                f = DateFormat.getTimeInstance(style, locale);
            }
            else
            {
                f = DateFormat.getDateTimeInstance(style, style, locale);
            }
        }
        return f.format(d);
    }
}
