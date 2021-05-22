package com.github.bannmann.puretemplate.debug;

import com.github.bannmann.puretemplate.InstanceScope;

public class EvalExprEvent extends InterpEvent {
    /** Index of first char in template. */
    public final int exprStartChar;
    /** Index of last char in template (inclusive). */
    public final int exprStopChar;
    public final String expr;
    public EvalExprEvent(InstanceScope scope, int start, int stop,
                         int exprStartChar, int exprStopChar)
    {
        super(scope, start, stop);
        this.exprStartChar = exprStartChar;
        this.exprStopChar = exprStopChar;
        if ( exprStartChar >=0 && exprStopChar >=0 ) {
            expr = scope.st.impl.template.substring(exprStartChar, exprStopChar +1);
        }
        else {
            expr = "";
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{" +
               "self=" + scope.st +
               ", expr='" + expr + '\'' +
               ", exprStartChar=" + exprStartChar +
               ", exprStopChar=" + exprStopChar +
               ", start=" + outputStartChar +
               ", stop=" + outputStopChar +
               '}';
    }

}
