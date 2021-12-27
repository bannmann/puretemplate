package org.puretemplate;

import org.puretemplate.diagnostics.Event;
import org.puretemplate.misc.Location;

final class EvalTemplateEvent extends AbstractInterpreterEvent
    implements Event.EvalTemplate, Distributable<Event.EvalTemplate>
{
    public EvalTemplateEvent(Location location, int exprStartChar, int exprStopChar)
    {
        super(location, exprStartChar, exprStopChar);
    }

    @Override
    public String toString()
    {
        return EvalTemplate.class.getSimpleName() +
            "{" +
            "location=" +
            location.toShortString() +
            ", start=" +
            outputStartChar +
            ", stop=" +
            outputStopChar +
            '}';
    }
}
