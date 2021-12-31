package org.puretemplate;

import java.io.Writer;

/**
 * Replacement for {@link java.io.StringWriter}.
 * <ul>
 *     <li>Uses {@link StringBuilder} instead of {@link StringBuffer} to avoid synchronization overhead.</li>
 *     <li>{@link #close()} does not declare an {@link java.io.IOException IOException} that it never throws anyway.
 *     </li>
 * </ul>
 */
class StringBuilderWriter extends Writer
{
    private final StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void write(int c)
    {
        stringBuilder.append(c);
    }

    @Override
    public void write(char[] cbuf)
    {
        stringBuilder.append(cbuf);
    }

    @Override
    public void write(String str)
    {
        stringBuilder.append(str);
    }

    @Override
    public void write(String str, int off, int len)
    {
        stringBuilder.append(str, off, len);
    }

    @Override
    public void write(char[] cbuf, int off, int len)
    {
        stringBuilder.append(cbuf, off, len);
    }

    @Override
    public Writer append(CharSequence csq)
    {
        stringBuilder.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end)
    {
        stringBuilder.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(char c)
    {
        stringBuilder.append(c);
        return this;
    }

    @Override
    public void flush()
    {
    }

    @Override
    public String toString()
    {
        return stringBuilder.toString();
    }

    @Override
    public void close()
    {

    }
}
