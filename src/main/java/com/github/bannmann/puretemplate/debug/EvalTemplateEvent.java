package com.github.bannmann.puretemplate.debug;

import com.github.bannmann.puretemplate.InstanceScope;

public class EvalTemplateEvent extends InterpEvent {
    public EvalTemplateEvent(InstanceScope scope, int exprStartChar, int exprStopChar) {
        super(scope, exprStartChar, exprStopChar);
    }
}
