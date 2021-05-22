package com.github.bannmann.puretemplate;

import java.util.ArrayList;
import java.util.List;

import com.github.bannmann.puretemplate.debug.EvalTemplateEvent;
import com.github.bannmann.puretemplate.debug.InterpEvent;
import com.github.bannmann.puretemplate.gui.STViz;

public class InstanceScope
{
    /**
     * Template that invoked us.
     */
    public final InstanceScope parent;
    /**
     * Template we're executing.
     */
    public final ST st;
    /**
     * Current instruction pointer.
     */
    public int ip;

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
    public List<InterpEvent> events = new ArrayList<InterpEvent>();

    /**
     * All templates evaluated and embedded in this {@link ST}. Used for tree view in {@link STViz}.
     */
    public List<EvalTemplateEvent> childEvalTemplateEvents = new ArrayList<EvalTemplateEvent>();

    public boolean earlyEval;

    public InstanceScope(InstanceScope parent, ST st)
    {
        this.parent = parent;
        this.st = st;
        this.earlyEval = parent != null && parent.earlyEval;
    }
}
