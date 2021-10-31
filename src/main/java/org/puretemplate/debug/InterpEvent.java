package org.puretemplate.debug;

import lombok.Getter;

import org.apiguardian.api.API;
import org.puretemplate.misc.Location;

@API(status = API.Status.EXPERIMENTAL)
public class InterpEvent
{
    @Getter
    protected final Location location;

    /**
     * Index of first char into output stream.
     */
    public final int outputStartChar;

    /**
     * Index of last char into output stream (inclusive).
     */
    public final int outputStopChar;

    public InterpEvent(Location location, int outputStartChar, int outputStopChar)
    {
        this.location = location;
        this.outputStartChar = outputStartChar;
        this.outputStopChar = outputStopChar;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() +
            "{" +
            "self=" +
            location.getReference() +
            ", start=" +
            outputStartChar +
            ", stop=" +
            outputStopChar +
            '}';
    }
}
