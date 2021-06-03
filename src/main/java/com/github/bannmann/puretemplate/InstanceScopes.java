package com.github.bannmann.puretemplate;

import java.util.LinkedList;
import java.util.List;

import com.github.bannmann.puretemplate.debug.EvalTemplateEvent;

public class InstanceScopes
{
    public static List<EvalTemplateEvent> getEvalTemplateEventStack(InstanceScope scope, boolean topdown)
    {
        List<EvalTemplateEvent> stack = new LinkedList<>();
        InstanceScope p = scope;
        while (p != null)
        {
            EvalTemplateEvent eval = (EvalTemplateEvent) p.events.get(p.events.size() - 1);
            if (topdown)
            {
                stack.add(0, eval);
            }
            else
            {
                stack.add(eval);
            }
            p = p.parent;
        }
        return stack;
    }

    /**
     * If an instance of <i>x</i> is enclosed in a <i>y</i> which is in a
     * <i>z</i>, return a {@code String} of these instance names in order from
     * topmost to lowest; here that would be {@code [z y x]}.
     */
    public static String getEnclosingInstanceStackString(InstanceScope scope)
    {
        List<ST> templates = getEnclosingInstanceStack(scope, true);
        StringBuilder buf = new StringBuilder();
        int i = 0;
        for (ST st : templates)
        {
            if (i > 0)
            {
                buf.append(" ");
            }
            buf.append(st.getName());
            i++;
        }
        return buf.toString();
    }

    public static List<ST> getEnclosingInstanceStack(InstanceScope scope, boolean topdown)
    {
        List<ST> stack = new LinkedList<>();
        InstanceScope p = scope;
        while (p != null)
        {
            if (topdown)
            {
                stack.add(0, p.st);
            }
            else
            {
                stack.add(p.st);
            }
            p = p.parent;
        }
        return stack;
    }

    public static List<InstanceScope> getScopeStack(InstanceScope scope, boolean topdown)
    {
        List<InstanceScope> stack = new LinkedList<>();
        InstanceScope p = scope;
        while (p != null)
        {
            if (topdown)
            {
                stack.add(0, p);
            }
            else
            {
                stack.add(p);
            }
            p = p.parent;
        }
        return stack;
    }
}
