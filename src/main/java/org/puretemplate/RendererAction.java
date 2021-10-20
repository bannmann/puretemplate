package org.puretemplate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Locale;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.puretemplate.error.ErrorListener;

@RequiredArgsConstructor
class RendererAction implements IRendererAction
{
    private final ST st;
    private final Locale locale;
    private final ErrorListener errorListener;

    private int lineWidth = STWriter.NO_WRAP;

    @Override
    public void withLineWrapping(int lineWidth)
    {
        if (lineWidth < 1)
        {
            throw new IllegalArgumentException("lineWidth must be >= 1");
        }
        this.lineWidth = lineWidth;
    }

    @Override
    public int intoFile(@NonNull File file) throws IOException
    {
        return intoFile(file.toPath());
    }

    @Override
    public int intoFile(@NonNull File file, @NonNull Charset charset) throws IOException
    {
        return intoFile(file.toPath(), charset);
    }

    @Override
    public int intoFile(@NonNull Path file) throws IOException
    {
        return intoFile(file, StandardCharsets.UTF_8);
    }

    @Override
    public int intoFile(@NonNull Path file, @NonNull Charset charset, OpenOption... options) throws IOException
    {
        return intoOutputStream(Files.newOutputStream(file, options), charset);
    }

    @Override
    public int intoWriter(@NonNull Writer writer)
    {
        STWriter stWriter = new AutoIndentWriter(writer);
        stWriter.setLineWidth(lineWidth);
        return st.write(stWriter, locale, errorListener);
    }

    @Override
    public int intoOutputStream(@NonNull OutputStream outputStream)
    {
        return intoOutputStream(outputStream, StandardCharsets.UTF_8);
    }

    @Override
    public int intoOutputStream(@NonNull OutputStream outputStream, @NonNull Charset charset)
    {
        return intoWriter(new OutputStreamWriter(outputStream, charset));
    }

    @Override
    public String intoString()
    {
        StringWriter result = new StringWriter();
        intoWriter(result);
        return result.toString();
    }
}
