package com.github.bannmann.puretemplate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestIndentation extends BaseTest
{
    @Test
    public void testIndentInFrontOfTwoExpr() throws Exception
    {
        String templates = "list(a,b) ::= <<" + "  <a><b>" + newline + ">>" + newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        ST t = group.getInstanceOf("list");
        t.impl.dump();
        t.add("a", "Terence");
        t.add("b", "Jim");
        String expecting = "  TerenceJim";
        assertEquals(expecting, t.render());
    }

    @Test
    public void testSimpleIndentOfAttributeList() throws Exception
    {
        String templates = "list(names) ::= <<" + "  <names; separator=\"\\n\">" + newline + ">>" + newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        ST t = group.getInstanceOf("list");
        t.add("names", "Terence");
        t.add("names", "Jim");
        t.add("names", "Sriram");
        String expecting = "  Terence" + newline + "  Jim" + newline + "  Sriram";
        assertEquals(expecting, t.render());
    }

    @Test
    public void testIndentOfMultilineAttributes() throws Exception
    {
        String templates = "list(names) ::= <<" + "  <names; separator=\"\n\">" + newline + ">>" + newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        ST t = group.getInstanceOf("list");
        t.add("names", "Terence\nis\na\nmaniac");
        t.add("names", "Jim");
        t.add("names", "Sriram\nis\ncool");
        String expecting = "  Terence" +
            newline +
            "  is" +
            newline +
            "  a" +
            newline +
            "  maniac" +
            newline +
            "  Jim" +
            newline +
            "  Sriram" +
            newline +
            "  is" +
            newline +
            "  cool";
        assertEquals(expecting, t.render());
    }

    @Test
    public void testIndentOfMultipleBlankLines() throws Exception
    {
        String templates = "list(names) ::= <<" + "  <names>" + newline + ">>" + newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        ST t = group.getInstanceOf("list");
        t.add("names", "Terence\n\nis a maniac");
        String expecting = "  Terence" + newline + "" + newline + // no indent on blank line
            "  is a maniac";
        assertEquals(expecting, t.render());
    }

    @Test
    public void testIndentBetweenLeftJustifiedLiterals() throws Exception
    {
        String templates = "list(names) ::= <<" +
            "Before:" +
            newline +
            "  <names; separator=\"\\n\">" +
            newline +
            "after" +
            newline +
            ">>" +
            newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        ST t = group.getInstanceOf("list");
        t.add("names", "Terence");
        t.add("names", "Jim");
        t.add("names", "Sriram");
        String expecting = "Before:" +
            newline +
            "  Terence" +
            newline +
            "  Jim" +
            newline +
            "  Sriram" +
            newline +
            "after";
        assertEquals(expecting, t.render());
    }

    @Test
    public void testNestedIndent() throws Exception
    {
        String templates = "method(name,stats) ::= <<" +
            "void <name>() {" +
            newline +
            "\t<stats; separator=\"\\n\">" +
            newline +
            "}" +
            newline +
            ">>" +
            newline +
            "ifstat(expr,stats) ::= <<" +
            newline +
            "if (<expr>) {" +
            newline +
            "  <stats; separator=\"\\n\">" +
            newline +
            "}" +
            ">>" +
            newline +
            "assign(lhs,expr) ::= \"<lhs>=<expr>;\"" +
            newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        ST t = group.getInstanceOf("method");
        t.add("name", "foo");
        ST s1 = group.getInstanceOf("assign");
        s1.add("lhs", "x");
        s1.add("expr", "0");
        ST s2 = group.getInstanceOf("ifstat");
        s2.add("expr", "x>0");
        ST s2a = group.getInstanceOf("assign");
        s2a.add("lhs", "y");
        s2a.add("expr", "x+y");
        ST s2b = group.getInstanceOf("assign");
        s2b.add("lhs", "z");
        s2b.add("expr", "4");
        s2.add("stats", s2a);
        s2.add("stats", s2b);
        t.add("stats", s1);
        t.add("stats", s2);
        String expecting = "void foo() {" +
            newline +
            "\tx=0;" +
            newline +
            "\tif (x>0) {" +
            newline +
            "\t  y=x+y;" +
            newline +
            "\t  z=4;" +
            newline +
            "\t}" +
            newline +
            "}";
        assertEquals(expecting, t.render());
    }

    @Test
    public void testIndentedIFWithValueExpr() throws Exception
    {
        ST t = new ST("begin" + newline + "    <if(x)>foo<endif>" + newline + "end" + newline);
        t.add("x", "x");
        String expecting = "begin" + newline + "    foo" + newline + "end" + newline;
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testIndentedIFWithElse() throws Exception
    {
        ST t = new ST("begin" + newline + "    <if(x)>foo<else>bar<endif>" + newline + "end" + newline);
        t.add("x", "x");
        String expecting = "begin" + newline + "    foo" + newline + "end" + newline;
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testIndentedIFWithElse2() throws Exception
    {
        ST t = new ST("begin" + newline + "    <if(x)>foo<else>bar<endif>" + newline + "end" + newline);
        t.add("x", false);
        String expecting = "begin" + newline + "    bar" + newline + "end" + newline;
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testIndentedIFWithNewlineBeforeText() throws Exception
    {
        STGroup group = new STGroup();
        group.defineTemplate("t", "x", "begin" + newline + "    <if(x)>\n" + "foo\n" +  // no indent; ignore IF indent
            "    <endif>" + newline +    // ignore indent on if-tags on line by themselves
            "end" + newline);
        ST t = group.getInstanceOf("t");
        t.add("x", "x");
        String expecting = "begin" + newline + "foo" + newline + "end";
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testIndentedIFWithEndifNextLine() throws Exception
    {
        STGroup group = new STGroup();
        group.defineTemplate("t", "x", "begin" + newline + "    <if(x)>foo\n" +      // use indent and keep newline
            "    <endif>" + newline +    // ignore indent on if-tags on line by themselves
            "end" + newline);
        ST t = group.getInstanceOf("t");
        t.add("x", "x");
        String expecting = "begin" + newline + "    foo" + newline + "end";
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testIFWithIndentOnMultipleLines() throws Exception
    {
        ST t = new ST("begin" +
            newline +
            "   <if(x)>" +
            newline +
            "   foo" +
            newline +
            "   <else>" +
            newline +
            "   bar" +
            newline +
            "   <endif>" +
            newline +
            "end" +
            newline);
        String expecting = "begin" + newline + "   bar" + newline + "end" + newline;
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testIFWithIndentAndExprOnMultipleLines() throws Exception
    {
        ST t = new ST("begin" +
            newline +
            "   <if(x)>" +
            newline +
            "   <x>" +
            newline +
            "   <else>" +
            newline +
            "   <y>" +
            newline +
            "   <endif>" +
            newline +
            "end" +
            newline);
        t.add("y", "y");
        String expecting = "begin" + newline + "   y" + newline + "end" + newline;
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testIFWithIndentAndExprWithIndentOnMultipleLines() throws Exception
    {
        ST t = new ST("begin" +
            newline +
            "   <if(x)>" +
            newline +
            "     <x>" +
            newline +
            "   <else>" +
            newline +
            "     <y>" +
            newline +
            "   <endif>" +
            newline +
            "end" +
            newline);
        t.add("y", "y");
        String expecting = "begin" + newline + "     y" + newline + "end" + newline;
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testNestedIFWithIndentOnMultipleLines() throws Exception
    {
        ST t = new ST("begin" +
            newline +
            "   <if(x)>" +
            newline +
            "      <if(y)>" +
            newline +
            "      foo" +
            newline +
            "      <endif>" +
            newline +
            "   <else>" +
            newline +
            "      <if(z)>" +
            newline +
            "      foo" +
            newline +
            "      <endif>" +
            newline +
            "   <endif>" +
            newline +
            "end" +
            newline);
        t.add("x", "x");
        t.add("y", "y");
        String expecting = "begin" + newline + "      foo" + newline + "end" + newline; // no indent
        String result = t.render();
        assertEquals(expecting, result);
    }

    @Test
    public void testIFInSubtemplate() throws Exception
    {
        ST t = new ST("<names:{n |" +
            newline +
            "   <if(x)>" +
            newline +
            "   <x>" +
            newline +
            "   <else>" +
            newline +
            "   <y>" +
            newline +
            "   <endif>" +
            newline +
            "}>" +
            newline);
        t.add("names", "Ter");
        t.add("y", "y");
        String expecting = "   y" + newline + newline;
        String result = t.render();
        assertEquals(expecting, result);
    }
}
