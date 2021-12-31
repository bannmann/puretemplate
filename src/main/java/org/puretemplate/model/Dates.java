package org.puretemplate.model;

import static java.util.Map.entry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import lombok.experimental.UtilityClass;

@UtilityClass
class Dates
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

    public String format(Date date, String formatString, Locale locale)
    {
        DateFormat dateFormat = getDateFormat(formatString, locale);
        return dateFormat.format(date);
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
