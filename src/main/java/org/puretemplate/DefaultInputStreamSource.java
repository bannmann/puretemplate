package org.puretemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
class DefaultInputStreamSource implements InputStreamSource
{
    @NonNull
    @Getter
    String name;

    @NonNull InputSupplier<InputStream> inputStreamSupplier;

    @NonNull
    @With
    Charset charset;

    @Override
    public Reader open() throws IOException
    {
        return new InputStreamReader(inputStreamSupplier.get(), charset);
    }
}
