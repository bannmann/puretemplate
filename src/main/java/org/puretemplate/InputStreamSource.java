package org.puretemplate;

import java.nio.charset.Charset;

interface InputStreamSource extends Source
{
    InputStreamSource withCharset(Charset charset);
}
