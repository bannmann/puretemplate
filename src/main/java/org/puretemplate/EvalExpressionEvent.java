package org.puretemplate;

import org.puretemplate.diagnostics.Event;
import org.puretemplate.misc.Location;

final class EvalExpressionEvent extends AbstractExpressionEvent
    implements Event.EvalExpression, Distributable<Event.EvalExpression>
{
    public EvalExpressionEvent(Location location, int start, int stop, int exprStartChar, int exprStopChar)
    {
        super(location, start, stop, exprStartChar, exprStopChar);
    }

    @Override
    public String toString()
    {
        return EvalExpression.class.getSimpleName() + "{" + getToStringDetails() + '}';
    }
}
