package com.example;

import java.io.IOException;

import org.junit.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;

public class TestIndentation extends BaseTest
{
    @Test
    public void testSimpleIndentOfAttributeList()
    {
        String templates = "list(names) ::= <<" + "  <names; separator=\"\\n\">" + NEWLINE + ">>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("list")
            .createContext()
            .add("names", "Terence")
            .add("names", "Jim")
            .add("names", "Sriram");

        assertRenderingResult("  Terence" + NEWLINE + "  Jim" + NEWLINE + "  Sriram", context);
    }

    @Test
    public void testIndentOfMultilineAttributes()
    {
        String templates = "list(names) ::= <<" + "  <names; separator=\"\n\">" + NEWLINE + ">>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("list")
            .createContext()
            .add("names", "Terence\nis\na\nmaniac")
            .add("names", "Jim")
            .add("names", "Sriram\nis\ncool");

        assertRenderingResult("  Terence" +
            NEWLINE +
            "  is" +
            NEWLINE +
            "  a" +
            NEWLINE +
            "  maniac" +
            NEWLINE +
            "  Jim" +
            NEWLINE +
            "  Sriram" +
            NEWLINE +
            "  is" +
            NEWLINE +
            "  cool", context);
    }

    @Test
    public void testIndentOfMultipleBlankLines()
    {
        String templates = "list(names) ::= <<" + "  <names>" + NEWLINE + ">>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("list")
            .createContext()
            .add("names", "Terence\n\nis a maniac");

        assertRenderingResult("  Terence" + NEWLINE + "" + NEWLINE + // no indent on blank line
            "  is a maniac", context);
    }

    @Test
    public void testIndentBetweenLeftJustifiedLiterals()
    {
        String templates = "list(names) ::= <<" +
            "Before:" +
            NEWLINE +
            "  <names; separator=\"\\n\">" +
            NEWLINE +
            "after" +
            NEWLINE +
            ">>" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("list")
            .createContext()
            .add("names", "Terence")
            .add("names", "Jim")
            .add("names", "Sriram");

        assertRenderingResult("Before:" +
            NEWLINE +
            "  Terence" +
            NEWLINE +
            "  Jim" +
            NEWLINE +
            "  Sriram" +
            NEWLINE +
            "after", context);
    }

    @Test
    public void testNestedIndent() throws IOException
    {
        String templates = "method(name,stats) ::= <<" +
            "void <name>() {" +
            NEWLINE +
            "\t<stats; separator=\"\\n\">" +
            NEWLINE +
            "}" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "ifstat(expr,stats) ::= <<" +
            NEWLINE +
            "if (<expr>) {" +
            NEWLINE +
            "  <stats; separator=\"\\n\">" +
            NEWLINE +
            "}" +
            ">>" +
            NEWLINE +
            "assign(lhs,expr) ::= \"<lhs>=<expr>;\"" +
            NEWLINE;

        Group group = loadGroupViaDisk(templates);

        Context t = group.getTemplate("method")
            .createContext()
            .add("name", "foo");

        Context s1 = group.getTemplate("assign")
            .createContext()
            .add("lhs", "x")
            .add("expr", "0");

        Context s2 = group.getTemplate("ifstat")
            .createContext()
            .add("expr", "x>0");

        Context s2a = group.getTemplate("assign")
            .createContext()
            .add("lhs", "y")
            .add("expr", "x+y");

        Context s2b = group.getTemplate("assign")
            .createContext()
            .add("lhs", "z")
            .add("expr", "4");

        s2.add("stats", s2a)
            .add("stats", s2b);

        t.add("stats", s1)
            .add("stats", s2);

        assertRenderingResult("void foo() {" +
            NEWLINE +
            "\tx=0;" +
            NEWLINE +
            "\tif (x>0) {" +
            NEWLINE +
            "\t  y=x+y;" +
            NEWLINE +
            "\t  z=4;" +
            NEWLINE +
            "\t}" +
            NEWLINE +
            "}", t);
    }

    @Test
    public void testIndentedIFWithValueExpr()
    {
        Context context = makeTemplateContext("begin" +
            NEWLINE +
            "    <if(x)>foo<endif>" +
            NEWLINE +
            "end" +
            NEWLINE).add("x", "x");
        assertRenderingResult("begin" + NEWLINE + "    foo" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    public void testIndentedIFWithElse()
    {
        Context context = makeTemplateContext("begin" +
            NEWLINE +
            "    <if(x)>foo<else>bar<endif>" +
            NEWLINE +
            "end" +
            NEWLINE).add("x", "x");
        assertRenderingResult("begin" + NEWLINE + "    foo" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    public void testIndentedIFWithElse2()
    {
        Context context = makeTemplateContext("begin" +
            NEWLINE +
            "    <if(x)>foo<else>bar<endif>" +
            NEWLINE +
            "end" +
            NEWLINE).add("x", false);
        assertRenderingResult("begin" + NEWLINE + "    bar" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    public void testIndentedIFWithNewlineBeforeText()
    {
        String templates = "t(x) ::= <<begin" + NEWLINE + "    <if(x)>\n" + "foo\n" + // no indent; ignore IF indent
            "    <endif>" + NEWLINE + // ignore indent on if-tags on line by themselves
            "end>>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("t")
            .createContext()
            .add("x", "x");

        assertRenderingResult("begin" + NEWLINE + "foo" + NEWLINE + "end", context);
    }

    @Test
    public void testIndentedIFWithEndifNextLine() throws IOException
    {
        String templates = "t(x) ::= <<begin" +
            NEWLINE +
            "    <if(x)>foo\n" +
            "    <endif>" +
            NEWLINE +
            "end" +
            NEWLINE +
            ">>";

        Context context = loadGroupViaDisk(templates).getTemplate("t")
            .createContext()
            .add("x", "x");

        assertRenderingResult("begin" + NEWLINE + "    foo" + NEWLINE + "end", context);
    }

    @Test
    public void testIFWithIndentOnMultipleLines()
    {
        String template = "begin" +
            NEWLINE +
            "   <if(x)>" +
            NEWLINE +
            "   foo" +
            NEWLINE +
            "   <else>" +
            NEWLINE +
            "   bar" +
            NEWLINE +
            "   <endif>" +
            NEWLINE +
            "end" +
            NEWLINE;
        assertNoArgRenderingResult("begin" + NEWLINE + "   bar" + NEWLINE + "end" + NEWLINE, template);
    }

    @Test
    public void testIFWithIndentAndExprOnMultipleLines()
    {
        Context context = makeTemplateContext("begin" +
            NEWLINE +
            "   <if(x)>" +
            NEWLINE +
            "   <x>" +
            NEWLINE +
            "   <else>" +
            NEWLINE +
            "   <y>" +
            NEWLINE +
            "   <endif>" +
            NEWLINE +
            "end" +
            NEWLINE).add("y", "y");
        assertRenderingResult("begin" + NEWLINE + "   y" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    public void testIFWithIndentAndExprWithIndentOnMultipleLines()
    {
        Context context = makeTemplateContext("begin" +
            NEWLINE +
            "   <if(x)>" +
            NEWLINE +
            "     <x>" +
            NEWLINE +
            "   <else>" +
            NEWLINE +
            "     <y>" +
            NEWLINE +
            "   <endif>" +
            NEWLINE +
            "end" +
            NEWLINE).add("y", "y");
        assertRenderingResult("begin" + NEWLINE + "     y" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    public void testNestedIFWithIndentOnMultipleLines()
    {
        Context context = makeTemplateContext("begin" +
            NEWLINE +
            "   <if(x)>" +
            NEWLINE +
            "      <if(y)>" +
            NEWLINE +
            "      foo" +
            NEWLINE +
            "      <endif>" +
            NEWLINE +
            "   <else>" +
            NEWLINE +
            "      <if(z)>" +
            NEWLINE +
            "      foo" +
            NEWLINE +
            "      <endif>" +
            NEWLINE +
            "   <endif>" +
            NEWLINE +
            "end" +
            NEWLINE).add("x", "x")
            .add("y", "y");
        assertRenderingResult("begin" + NEWLINE + "      foo" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    public void testIFInSubtemplate()
    {
        Context context = makeTemplateContext("<names:{n |" +
            NEWLINE +
            "   <if(x)>" +
            NEWLINE +
            "   <x>" +
            NEWLINE +
            "   <else>" +
            NEWLINE +
            "   <y>" +
            NEWLINE +
            "   <endif>" +
            NEWLINE +
            "}>" +
            NEWLINE).add("names", "Ter")
            .add("y", "y");
        assertRenderingResult("   y" + NEWLINE + NEWLINE, context);
    }
}
