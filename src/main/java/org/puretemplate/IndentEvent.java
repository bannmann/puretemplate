package org.puretemplate;

import org.puretemplate.misc.Location;

class IndentEvent extends EvalExprEvent
{
    public IndentEvent(Location location, int start, int stop, int exprStartChar, int exprStopChar)
    {
        super(location, start, stop, exprStartChar, exprStopChar);
    }
}
