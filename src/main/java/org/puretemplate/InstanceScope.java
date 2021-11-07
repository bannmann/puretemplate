package org.puretemplate;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Includes the {@link EvalTemplateEvent} for this template. This is a subset of {@link Interpreter#events} field.
     * The final {@link EvalTemplateEvent} is stored in 3 places:
     *
     * <ol>
     *  <li>In {@link #parent}'s {@link #childEvalTemplateEvents} list</li>
     *  <li>In this list</li>
     *  <li>In the {@link Interpreter#events} list</li>
     * </ol>
     *
     * The root ST has the final {@link EvalTemplateEvent} in its list.
     * <p>
     * All events get added to the {@link #parent}'s event list.</p>
     */
    List<InterpEvent> events = new ArrayList<>();

    /**
     * All templates evaluated and embedded in this {@link ST}. Used for tree view in {@link STViz}.
     */
    List<EvalTemplateEvent> childEvalTemplateEvents = new ArrayList<>();

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
