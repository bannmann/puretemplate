package org.puretemplate.debug;

import org.apiguardian.api.API;
import org.puretemplate.misc.Location;

@API(status = API.Status.EXPERIMENTAL)
public class EvalTemplateEvent extends InterpEvent
{
    public EvalTemplateEvent(Location location, int exprStartChar, int exprStopChar)
    {
        super(location, exprStartChar, exprStopChar);
    }
}
