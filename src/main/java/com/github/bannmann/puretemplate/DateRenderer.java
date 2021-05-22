package com.github.bannmann.puretemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A renderer for {@link Date} and {@link Calendar} objects. It understands a
 * variety of format names as shown in {@link #formatToInt} field. By default it
 * assumes {@code "short"} format. A prefix of {@code "date:"} or
 * {@code "time:"} shows only those components of the time object.
 */
// using <Object> because this can handle Date and Calendar objects, which don't have a common supertype.
public class DateRenderer implements AttributeRenderer<Object> {
    public static final Map<String, Integer> formatToInt;

    static {
        final Map<String, Integer> map = new HashMap<String, Integer>();

        map.put("short", DateFormat.SHORT);
        map.put("medium", DateFormat.MEDIUM);
        map.put("long", DateFormat.LONG);
        map.put("full", DateFormat.FULL);

        map.put("date:short", DateFormat.SHORT);
        map.put("date:medium", DateFormat.MEDIUM);
        map.put("date:long", DateFormat.LONG);
        map.put("date:full", DateFormat.FULL);

        map.put("time:short", DateFormat.SHORT);
        map.put("time:medium", DateFormat.MEDIUM);
        map.put("time:long", DateFormat.LONG);
        map.put("time:full", DateFormat.FULL);

        formatToInt = Collections.unmodifiableMap(map);
    }

    @Override
    public String toString(Object value, String formatString, Locale locale) {
        Date d;
        if ( formatString==null ) formatString = "short";
        if ( value instanceof Calendar ) d = ((Calendar)value).getTime();
        else d = (Date)value;
        Integer styleI = formatToInt.get(formatString);
        DateFormat f;
        if ( styleI==null ) f = new SimpleDateFormat(formatString, locale);
        else {
            int style = styleI.intValue();
            if ( formatString.startsWith("date:") ) f = DateFormat.getDateInstance(style, locale);
            else if ( formatString.startsWith("time:") ) f = DateFormat.getTimeInstance(style, locale);
            else f = DateFormat.getDateTimeInstance(style, style, locale);
        }
        return f.format(d);
    }
}
