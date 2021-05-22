package com.github.bannmann.puretemplate.misc;

/**
 * A line number and char position within a line.  Used by the source mapping stuff to map address to range within a
 * template.
 */
public class Coordinate
{
    public int line;
    public int charPosition;

    public Coordinate(int a, int b)
    {
        this.line = a;
        this.charPosition = b;
    }

    @Override
    public String toString()
    {
        return line + ":" + charPosition;
    }
}
