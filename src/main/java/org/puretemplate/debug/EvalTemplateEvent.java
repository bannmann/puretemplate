package org.puretemplate.debug;

import org.puretemplate.misc.Location;

public class EvalTemplateEvent extends InterpEvent
{
    public EvalTemplateEvent(Location location, int exprStartChar, int exprStopChar)
    {
        super(location, exprStartChar, exprStopChar);
    }
}
