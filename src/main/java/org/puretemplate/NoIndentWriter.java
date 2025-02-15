package org.puretemplate;

import java.io.IOException;
import java.io.Writer;

/**
 * Just pass through the text.
 */
class NoIndentWriter extends AutoIndentWriter
{
    public NoIndentWriter(Writer out)
    {
        super(out);
    }

    @Override
    public int write(String str) throws IOException
    {
        out.write(str);
        return str.length();
    }

    @Override
    public TemplateWriter createWriterTargeting(Writer target)
    {
        return new NoIndentWriter(target);
    }
}
