package org.puretemplate;

import java.io.IOException;
import java.io.Reader;

interface Source
{
    String getName();

    Reader open() throws IOException;
}
