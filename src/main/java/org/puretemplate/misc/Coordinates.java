package org.puretemplate.misc;

import java.io.Serializable;

import lombok.Value;

/**
 * A line number and char position within a line.  Used by the source mapping stuff to map address to range within a
 * template.
 */
@Value
public class Coordinates implements Serializable
{
    /**
     * Given {@code index} into string {@code s}, compute the line and char position in line.
     */
    public static Coordinates getLineCharPosition(String s, int index)
    {
        int line = 1;
        int charPos = 0;
        int p = 0;
        while (p < index)
        {
            // don't care about s[index] itself; count before
            if (s.charAt(p) == '\n')
            {
                line++;
                charPos = 0;
            }
            else
            {
                charPos++;
            }
            p++;
        }

        return new Coordinates(line, charPos);
    }

    int line;
    int charPosition;

    @Override
    public String toString()
    {
        return line + ":" + charPosition;
    }
}
