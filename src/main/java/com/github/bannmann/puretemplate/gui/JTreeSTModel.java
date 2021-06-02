package com.github.bannmann.puretemplate.gui;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.github.bannmann.puretemplate.Interpreter;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.StringRenderer;
import com.github.bannmann.puretemplate.debug.EvalTemplateEvent;

public class JTreeSTModel implements TreeModel
{
    public Interpreter interp;
    public Wrapper root;

    public static final class Wrapper
    {
        EvalTemplateEvent event;

        public Wrapper(EvalTemplateEvent event)
        {
            this.event = event;
        }

        @Override
        public int hashCode()
        {
            return event.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Wrapper))
            {
                return false;
            }
            return this.event == ((Wrapper) o).event;
        }

        @Override
        public String toString()
        {
            ST st = event.scope.st;
            if (st.isAnonSubtemplate())
            {
                return "{...}";
            }
            if (st.debugState != null && st.debugState.newSTEvent != null)
            {
                String label = st.toString() +
                    " @ " +
                    st.debugState.newSTEvent.getFileName() +
                    ":" +
                    st.debugState.newSTEvent.getLine();
                return "<html><b>" + StringRenderer.escapeHTML(label) + "</b></html>";
            }
            else
            {
                return st.toString();
            }
        }
    }

    public JTreeSTModel(Interpreter interp, EvalTemplateEvent root)
    {
        this.interp = interp;
        this.root = new Wrapper(root);
    }

    @Override
    public Object getChild(Object parent, int index)
    {
        EvalTemplateEvent e = ((Wrapper) parent).event;
        return new Wrapper(e.scope.childEvalTemplateEvents.get(index));
    }

    @Override
    public int getChildCount(Object parent)
    {
        EvalTemplateEvent e = ((Wrapper) parent).event;
        return e.scope.childEvalTemplateEvents.size();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        EvalTemplateEvent p = ((Wrapper) parent).event;
        EvalTemplateEvent c = ((Wrapper) parent).event;
        int i = 0;
        for (EvalTemplateEvent e : p.scope.childEvalTemplateEvents)
        {
            if (e.scope.st == c.scope.st)
            {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return getChildCount(node) == 0;
    }

    @Override
    public Object getRoot()
    {
        return root;
    }

    @Override
    public void valueForPathChanged(TreePath treePath, Object o)
    {
    }

    @Override
    public void addTreeModelListener(TreeModelListener treeModelListener)
    {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener treeModelListener)
    {
    }
}
