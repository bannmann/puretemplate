package org.puretemplate;

import org.puretemplate.diagnostics.Event;
import org.puretemplate.misc.Location;

final class IndentEvent extends AbstractExpressionEvent implements Event.Indent, Distributable<Event.Indent>
{
    public IndentEvent(Location location, int start, int stop, int exprStartChar, int exprStopChar)
    {
        super(location, start, stop, exprStartChar, exprStopChar);
    }

    @Override
    public String toString()
    {
        return Indent.class.getSimpleName() + "{" + getToStringDetails() + '}';
    }
}
