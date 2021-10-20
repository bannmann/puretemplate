package org.puretemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.google.common.io.CharStreams;

abstract class AbstractLoaderAction
{
    private static final DelimiterConfig DEFAULT_DELIMITER_CONFIG = new DelimiterConfig('<', '>');

    protected Source source;
    protected Path file;
    protected DelimiterConfig delimiterConfig = DEFAULT_DELIMITER_CONFIG;

    protected static String loadFrom(Source source)
    {
        try (Reader reader = source.open())
        {
            return CharStreams.toString(reader);
        }
        catch (IOException e)
        {
            // TODO should we throw a regular IOException (for consistency with other I/O APIs)?
            throw new UncheckedIOException(e);
        }
    }

    public void fromFile(String filePath)
    {
        fromFile(Path.of(filePath));
    }

    public void fromFile(File file)
    {
        fromFile(file.toPath());
    }

    public void fromFile(Path file)
    {
        this.source = new FileSource(file, StandardCharsets.UTF_8);
        this.file = file;
    }

    public void fromInputStream(InputSupplier<InputStream> inputStreamSupplier)
    {
        source = new DefaultInputStreamSource("<stream>", inputStreamSupplier, StandardCharsets.UTF_8);
    }

    public void fromInputStream(InputStream inputStream)
    {
        fromInputStream(() -> inputStream);
    }

    public void fromReader(InputSupplier<Reader> readerSupplier)
    {
        this.source = new ReaderSource("<reader>", readerSupplier);
    }

    public void fromReader(Reader reader)
    {
        fromReader(() -> reader);
    }

    public void fromResourceFile(Class<?> reference, String relativePath)
    {
        fromFile(Resources.get(reference, relativePath));
    }

    public void fromResourceFile(ClassLoader classLoader, String absolutePath)
    {
        fromFile(Resources.get(classLoader, absolutePath));
    }

    public void fromResourceFile(String absolutePath)
    {
        fromFile(Resources.get(absolutePath));
    }

    public void fromString(String source)
    {
        this.source = new ReaderSource("<string>", () -> new StringReader(source));
    }

    public void usingCharset(Charset charset)
    {
        if (source instanceof InputStreamSource)
        {
            this.source = ((InputStreamSource) this.source).withCharset(charset);
        }
    }

    public void withDelimiters(char start, char stop)
    {
        delimiterConfig = new DelimiterConfig(start, stop);
    }
}
