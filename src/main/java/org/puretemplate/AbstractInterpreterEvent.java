package org.puretemplate;

import lombok.Getter;

import org.puretemplate.misc.Location;

abstract class AbstractInterpreterEvent extends AbstractEvent
{
    @Getter
    protected final Location location;

    /**
     * Index of first char into output stream.
     */
    protected final int outputStartChar;

    /**
     * Index of last char into output stream (inclusive).
     */
    protected final int outputStopChar;

    public AbstractInterpreterEvent(Location location, int outputStartChar, int outputStopChar)
    {
        this.location = location;
        this.outputStartChar = outputStartChar;
        this.outputStopChar = outputStopChar;
    }
}
