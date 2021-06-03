package com.github.bannmann.puretemplate.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.antlr.runtime.tree.CommonTree;

import com.github.bannmann.puretemplate.InstanceScope;
import com.github.bannmann.puretemplate.InstanceScopes;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.StringRenderer;
import com.github.bannmann.puretemplate.debug.AddAttributeEvent;

/**
 * From a scope, get stack of enclosing scopes in order from root down to scope. Then show each scope's (ST's)
 * attributes as children.
 */
public final class JTreeScopeStackModel implements TreeModel
{
    CommonTree root;

    public static class StringTree extends CommonTree
    {
        String text;

        public StringTree(String text)
        {
            this.text = text;
        }

        @Override
        public boolean isNil()
        {
            return text == null;
        }

        @Override
        public String toString()
        {
            if (!isNil())
            {
                return text;
            }
            return "nil";
        }
    }

    public JTreeScopeStackModel(InstanceScope scope)
    {
        root = new StringTree("Scope stack:");
        List<InstanceScope> stack = InstanceScopes.getScopeStack(scope, false);
        for (InstanceScope s : stack)
        {
            StringTree templateNode = new StringTree(s.st.getName());
            root.insertChild(0, templateNode);

            Set<String> names = new HashSet<>();
            addAttributeDescriptions(s.st, templateNode, names);
        }
    }

    public void addAttributeDescriptions(ST st, StringTree node, Set<String> names)
    {
        Map<String, Object> attrs = st.getAttributes();
        if (attrs == null)
        {
            return;
        }
        for (Map.Entry<String, Object> entry : attrs.entrySet())
        {
            String name = entry.getKey();
            Object value = entry.getValue();
            String description;
            if (st.debugState != null && st.debugState.addAttrEvents != null)
            {
                StringBuilder locations = new StringBuilder();
                int i = 0;
                for (AddAttributeEvent ae : st.debugState.addAttrEvents.get(name))
                {
                    if (i > 0)
                    {
                        locations.append(", ");
                    }
                    locations.append(ae.getFileName() + ":" + ae.getLine());
                    i++;
                }
                if (locations.length() > 0)
                {
                    description = name + " = " + value + " @ " + locations;
                }
                else
                {
                    description = name + " = " + value;
                }
            }
            else
            {
                description = name + " = " + value;
            }

            if (!names.add(name))
            {
                description = String.format("<html><font color=\"gray\">%s</font></html>",
                    StringRenderer.escapeHTML(description));
            }

            node.addChild(new StringTree(description));
        }
    }

    @Override
    public Object getRoot()
    {
        return root;
    }

    @Override
    public Object getChild(Object parent, int i)
    {
        StringTree t = (StringTree) parent;
        return t.getChild(i);
    }

    @Override
    public int getChildCount(Object parent)
    {
        StringTree t = (StringTree) parent;
        return t.getChildCount();
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return getChildCount(node) == 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        StringTree c = (StringTree) child;
        return c.getChildIndex();
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
