package org.puretemplate;

import java.io.IOException;

interface InputSupplier<T>
{
    T get() throws IOException;
}
