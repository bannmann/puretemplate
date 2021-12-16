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
    private final @NonNull ST st;
    private final @NonNull Locale locale;
    private final ErrorListener errorListener;

    private int lineWidth = STWriter.NO_WRAP;

    /**
     * @return the next state of the fluent API. See <a href="../package-summary.html#fluent-api-usage-notes">Usage
     * notes for fluent APIs in PureTemplate</a> for details.
     */
    @Override
    public void withLineWrapping(int lineWidth)
    {
        if (lineWidth < 1)
        {
            throw new IllegalArgumentException("lineWidth must be >= 1");
        }
        this.lineWidth = lineWidth;
    }

    /**
     * @return the number of bytes written
     */
    @Override
    public int intoFile(@NonNull File file) throws IOException
    {
        return intoFile(file.toPath());
    }

    /**
     * @return the number of bytes written
     */
    @Override
    public int intoFile(@NonNull File file, @NonNull Charset charset) throws IOException
    {
        return intoFile(file.toPath(), charset);
    }

    /**
     * @return the number of bytes written
     */
    @Override
    public int intoFile(@NonNull Path file) throws IOException
    {
        return intoFile(file, StandardCharsets.UTF_8);
    }

    /**
     * @return the number of bytes written
     */
    @Override
    public int intoFile(@NonNull Path file, @NonNull Charset charset, OpenOption... options) throws IOException
    {
        return intoOutputStream(Files.newOutputStream(file, options), charset);
    }

    /**
     * @return the number of bytes written
     */
    @Override
    public int intoWriter(@NonNull Writer writer)
    {
        STWriter stWriter = new AutoIndentWriter(writer);
        stWriter.setLineWidth(lineWidth);
        return st.write(stWriter, locale, errorListener);
    }

    /**
     * @return the number of bytes written
     */
    @Override
    public int intoOutputStream(@NonNull OutputStream outputStream)
    {
        return intoOutputStream(outputStream, StandardCharsets.UTF_8);
    }

    /**
     * @return the number of bytes written
     */
    @Override
    public int intoOutputStream(@NonNull OutputStream outputStream, @NonNull Charset charset)
    {
        return intoWriter(new OutputStreamWriter(outputStream, charset));
    }

    /**
     * @return the rendering result
     */
    @Override
    public String intoString()
    {
        StringWriter result = new StringWriter();
        intoWriter(result);
        return result.toString();
    }
}
