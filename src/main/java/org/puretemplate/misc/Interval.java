package org.puretemplate.misc;

import lombok.Value;

/**
 * An inclusive interval {@code a..b}.  Used to track ranges in output and template patterns (for debugging).
 */
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
