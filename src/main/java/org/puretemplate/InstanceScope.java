package org.puretemplate;

import org.puretemplate.misc.Coordinates;
import org.puretemplate.misc.Location;

class InstanceScope
{
    /**
     * Template that invoked us.
     */
    final InstanceScope parent;

    /**
     * Template we're executing.
     */
    final ST st;

    /**
     * Current instruction pointer.
     */
    int ip;

    boolean earlyEval;

    public InstanceScope(InstanceScope parent, ST st)
    {
        this.parent = parent;
        this.st = st;
        this.earlyEval = parent != null && parent.earlyEval;
    }

    public String getSourceText()
    {
        return st.getSourceText();
    }

    public Location toLocation()
    {
        Location parentLocation = null;
        if (parent != null)
        {
            parentLocation = parent.toLocation();
        }

        return new Location(parentLocation, st.getName(), getCoordinate(), getSourceText(), getReference());
    }

    private Coordinates getCoordinate()
    {
        Interval interval = st.getInterval(ip);
        if (interval == null)
        {
            return null;
        }

        // get left edge and get line/col
        int i = interval.getA();
        int line = 1;
        int charPos = 0;
        int p = 0;
        while (p < i)
        {
            // don't care about s[index] itself; count before
            if (st.getSourceText()
                .charAt(p) == '\n')
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

    public String getReference()
    {
        return st.toString();
    }
}
