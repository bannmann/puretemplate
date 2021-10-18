package org.puretemplate.misc;

import org.puretemplate.error.Message;

public class ErrorBufferAllErrors extends ErrorBuffer
{
    @Override
    public void runTimeError(Message msg)
    {
        errors.add(msg);
    }
}
