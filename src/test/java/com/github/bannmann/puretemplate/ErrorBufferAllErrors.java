package com.github.bannmann.puretemplate;

import com.github.bannmann.puretemplate.misc.ErrorBuffer;
import com.github.bannmann.puretemplate.misc.STMessage;

public class ErrorBufferAllErrors extends ErrorBuffer {
    @Override
    public void runTimeError(STMessage msg) {
        errors.add(msg);
    }
}
