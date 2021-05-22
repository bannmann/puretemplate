package com.github.bannmann.puretemplate.debug;

import com.github.bannmann.puretemplate.InstanceScope;

public class IndentEvent extends EvalExprEvent {
    public IndentEvent(InstanceScope scope, int start, int stop, int exprStartChar, int exprStopChar) {
        super(scope, start, stop, exprStartChar, exprStopChar);
    }
}
