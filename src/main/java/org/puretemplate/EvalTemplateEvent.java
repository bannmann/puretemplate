package org.puretemplate;

import org.puretemplate.misc.Location;

class EvalTemplateEvent extends InterpEvent
{
    public EvalTemplateEvent(Location location, int exprStartChar, int exprStopChar)
    {
        super(location, exprStartChar, exprStopChar);
    }
}
