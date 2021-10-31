package org.puretemplate.debug;

import org.apiguardian.api.API;
import org.puretemplate.misc.Location;

@API(status = API.Status.EXPERIMENTAL)
public class IndentEvent extends EvalExprEvent
{
    public IndentEvent(Location location, int start, int stop, int exprStartChar, int exprStopChar)
    {
        super(location, start, stop, exprStartChar, exprStopChar);
    }
}
