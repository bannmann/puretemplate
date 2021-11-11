package org.puretemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import lombok.NonNull;

import com.google.common.io.CharStreams;

abstract class AbstractLoaderAction
{
    private static final DelimiterConfig DEFAULT_DELIMITER_CONFIG = new DelimiterConfig('<', '>');

    protected Source source;
    protected Path file;
    protected DelimiterConfig delimiterConfig = DEFAULT_DELIMITER_CONFIG;

    protected static String loadFrom(@NonNull Source source)
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

    public void fromFile(@NonNull String filePath)
    {
        fromFile(Path.of(filePath));
    }

    public void fromFile(@NonNull File file)
    {
        fromFile(file.toPath());
    }

    public void fromFile(@NonNull Path file)
    {
        this.source = new FileSource(file, StandardCharsets.UTF_8);
        this.file = file;
    }

    public void fromInputStream(@NonNull InputSupplier<InputStream> inputStreamSupplier)
    {
        source = new DefaultInputStreamSource("<stream>", inputStreamSupplier, StandardCharsets.UTF_8);
    }

    public void fromInputStream(@NonNull InputStream inputStream)
    {
        fromInputStream(() -> inputStream);
    }

    public void fromReader(@NonNull InputSupplier<Reader> readerSupplier)
    {
        this.source = new ReaderSource("<reader>", readerSupplier);
    }

    public void fromReader(@NonNull Reader reader)
    {
        fromReader(() -> reader);
    }

    public void fromResourceFile(@NonNull Class<?> reference, @NonNull String relativePath)
    {
        fromFile(Resources.get(reference, relativePath));
    }

    public void fromResourceFile(@NonNull ClassLoader classLoader, @NonNull String absolutePath)
    {
        fromFile(Resources.get(classLoader, absolutePath));
    }

    public void fromResourceFile(@NonNull String absolutePath)
    {
        fromFile(Resources.get(absolutePath));
    }

    public void fromString(@NonNull String source)
    {
        this.source = new ReaderSource("<string>", () -> new StringReader(source));
    }

    public void withDelimiters(char start, char stop)
    {
        delimiterConfig = new DelimiterConfig(start, stop);
    }
}
