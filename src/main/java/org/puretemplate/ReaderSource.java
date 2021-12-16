package org.puretemplate;

import java.io.IOException;
import java.io.Reader;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import org.puretemplate.misc.InputSupplier;

@Value
class ReaderSource implements Source
{
    String name;

    @Getter(AccessLevel.NONE)
    InputSupplier<Reader> supplier;

    @Override
    public Reader open() throws IOException
    {
        return supplier.get();
    }
}
