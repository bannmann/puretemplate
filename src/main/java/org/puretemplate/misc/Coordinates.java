package org.puretemplate.misc;

import java.io.Serializable;

import lombok.Value;

import org.apiguardian.api.API;

/**
 * A line number and char position within a line. Used by the source mapping stuff to map address to range within a
 * template.
 */
@API(status = API.Status.STABLE)
@Value
public class Coordinates implements Serializable
{
    int line;
    int charPosition;

    @Override
    public String toString()
    {
        return line + ":" + charPosition;
    }
}
