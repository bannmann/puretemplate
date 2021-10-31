package org.puretemplate.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Formatter;
import java.util.Locale;

import org.apiguardian.api.API;

/**
 * Works with {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, and {@link BigInteger} as well as {@link
 * Float}, {@link Double}, and {@link BigDecimal}.  You pass in a format string suitable for {@link Formatter#format}.
 * <p>
 * For example, {@code %10d} emits a number as a decimal int padding to 10 char. This can even do {@code long} to {@code
 * Date} conversions using the format string.</p>
 */
@API(status = API.Status.STABLE)
public class NumberRenderer implements AttributeRenderer<Object>
{
    @Override
    public String toString(Object value, String formatString, Locale locale)
    {
        if (formatString == null)
        {
            return value.toString();
        }

        try (Formatter f = new Formatter(locale))
        {
            f.format(formatString, value);
            return f.toString();
        }
    }
}
