package org.puretemplate;

import org.puretemplate.misc.Location;

abstract class AbstractExpressionEvent extends AbstractInterpreterEvent
{
    /**
     * Index of first char in template.
     */
    public final int exprStartChar;

    /**
     * Index of last char in template (inclusive).
     */
    public final int exprStopChar;

    public final String expr;

    public AbstractExpressionEvent(Location location, int start, int stop, int exprStartChar, int exprStopChar)
    {
        super(location, start, stop);
        this.exprStartChar = exprStartChar;
        this.exprStopChar = exprStopChar;
        if (exprStartChar >= 0 && exprStopChar >= 0)
        {
            expr = location.getSourceText()
                .substring(exprStartChar, exprStopChar + 1);
        }
        else
        {
            expr = "";
        }
    }

    protected String getToStringDetails()
    {
        return "location=" +
            location.toShortString() +
            ", expr='" +
            expr +
            "', exprStartChar=" +
            exprStartChar +
            ", exprStopChar=" +
            exprStopChar +
            ", start=" +
            outputStartChar +
            ", stop=" +
            outputStopChar;
    }
}
