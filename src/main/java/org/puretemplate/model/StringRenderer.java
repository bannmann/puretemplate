package org.puretemplate.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apiguardian.api.API;

/**
 * This render knows to perform a few format operations on {@link String} objects:
 * <ul>
 *  <li>{@code upper}: Convert to upper case.</li>
 *  <li>{@code lower}: Convert to lower case.</li>
 *  <li>{@code cap}: Convert first character to upper case.</li>
 *  <li>{@code url-encode}:</li>
 *  <li>{@code xml-encode}:</li>
 * </ul>
 */
@API(status = API.Status.STABLE)
public class StringRenderer implements AttributeRenderer<String>
{
    @Override
    public String render(String value, String formatString, Locale locale)
    {
        if (formatString == null)
        {
            return value;
        }
        if (formatString.equals("upper"))
        {
            return value.toUpperCase(locale);
        }
        if (formatString.equals("lower"))
        {
            return value.toLowerCase(locale);
        }
        if (formatString.equals("cap"))
        {
            return (value.length() > 0)
                ? Character.toUpperCase(value.charAt(0)) + value.substring(1)
                : value;
        }
        if (formatString.equals("url-encode"))
        {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        }
        if (formatString.equals("xml-encode"))
        {
            return escapeHTML(value);
        }
        return String.format(locale, formatString, value);
    }

    public static String escapeHTML(String s)
    {
        if (s == null)
        {
            return null;
        }
        StringBuilder buf = new StringBuilder(s.length());
        int len = s.length();
        for (int i = 0; i < len; )
        {
            int c = s.codePointAt(i);
            switch (c)
            {
                case '&':
                    buf.append("&amp;");
                    break;
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '\r':
                case '\n':
                case '\t':
                    buf.append((char) c);
                    break;
                default:
                    boolean control = c < ' '; // 32
                    boolean aboveASCII = c > 126;
                    if (control || aboveASCII)
                    {
                        buf.append("&#");
                        buf.append(c);
                        buf.append(";");
                    }
                    else
                    {
                        buf.append((char) c);
                    }
            }
            i += Character.charCount(c);
        }
        return buf.toString();
    }
}
