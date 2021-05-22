package com.github.bannmann.puretemplate.debug;

import com.github.bannmann.puretemplate.InstanceScope;

public class InterpEvent {
    public InstanceScope scope;
    /** Index of first char into output stream. */
    public final int outputStartChar;
    /** Index of last char into output stream (inclusive). */
    public final int outputStopChar;
    public InterpEvent(InstanceScope scope, int outputStartChar, int outputStopChar) {
        this.scope = scope;
        this.outputStartChar = outputStartChar;
        this.outputStopChar = outputStopChar;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{" +
               "self=" + scope.st +
               ", start=" + outputStartChar +
               ", stop=" + outputStopChar +
               '}';
    }
}
