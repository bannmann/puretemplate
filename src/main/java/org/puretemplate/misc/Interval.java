package org.puretemplate.misc;

import lombok.Value;

import org.apiguardian.api.API;

/**
 * An inclusive interval {@code a..b}.  Used to track ranges in output and template patterns (for debugging).
 */
@API(status = API.Status.INTERNAL)
@Value
public class Interval
{
    int a;
    int b;

    @Override
    public String toString()
    {
        return a + ".." + b;
    }
}
