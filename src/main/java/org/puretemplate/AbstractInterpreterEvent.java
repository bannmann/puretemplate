package org.puretemplate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.puretemplate.misc.Location;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
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
}
