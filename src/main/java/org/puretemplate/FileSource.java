package org.puretemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
class FileSource implements InputStreamSource
{
    @NonNull Path file;

    @NonNull
    @With
    Charset charset;

    @Override
    public String getName()
    {
        return file.toString();
    }

    @Override
    public Reader open() throws IOException
    {
        return new InputStreamReader(Files.newInputStream(file), charset);
    }
}
