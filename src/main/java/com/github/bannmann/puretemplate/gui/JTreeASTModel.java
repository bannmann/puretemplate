package com.github.bannmann.puretemplate.gui;

import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

// TODO: copied from ANTLR v4; rm when upgraded to v4
public class JTreeASTModel implements TreeModel {
    TreeAdaptor adaptor;
    Object root;

    public JTreeASTModel(TreeAdaptor adaptor, Object root) {
        this.adaptor = adaptor;
        this.root = root;
    }

    public JTreeASTModel(Object root) {
        this.adaptor = new CommonTreeAdaptor();
        this.root = root;
    }

    @Override
    public int getChildCount(Object parent) {
        return adaptor.getChildCount(parent);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child){
        if ( parent==null ) return -1;
        return adaptor.getChildIndex(child);
    }

    @Override
    public Object getChild(Object parent, int index){
        return adaptor.getChild(parent, index);
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node)==0;
    }

    @Override
    public Object getRoot() { return root; }

    @Override
    public void valueForPathChanged(TreePath treePath, Object o) {
    }

    @Override
    public void addTreeModelListener(TreeModelListener treeModelListener) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener treeModelListener) {
    }
}
