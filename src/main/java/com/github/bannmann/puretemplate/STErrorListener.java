package com.github.bannmann.puretemplate;

import com.github.bannmann.puretemplate.misc.STMessage;

/** How to handle messages. */
public interface STErrorListener {
    void compileTimeError(STMessage msg);
    void runTimeError(STMessage msg);
    void IOError(STMessage msg);
    void internalError(STMessage msg);
}
