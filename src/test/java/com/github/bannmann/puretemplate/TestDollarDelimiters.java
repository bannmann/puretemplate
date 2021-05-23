package com.github.bannmann.puretemplate;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

public class TestDollarDelimiters extends BaseTest
{
    @Test
    public void testAttr() throws Exception
    {
        String template = "hi $name$!";
        ST st = new ST(template, '$', '$');
        st.add("name", "Ter");
        String expected = "hi Ter!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testParallelMap() throws Exception
    {
        STGroup group = new STGroup('$', '$');
        group.defineTemplate("test", "names,phones", "hi $names,phones:{n,p | $n$:$p$;}$");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        st.add("phones", "x5001");
        st.add("phones", "x5002");
        st.add("phones", "x5003");
        String expected = "hi Ter:x5001;Tom:x5002;Sumana:x5003;";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testRefToAnotherTemplateInSameGroup() throws Exception
    {
        String dir = getRandomDir();
        String a = "a() ::= << <$b()$> >>\n";
        String b = "b() ::= <<bar>>\n";
        writeFile(dir, "a.st", a);
        writeFile(dir, "b.st", b);
        STGroup group = new STGroupDir(dir, '$', '$');
        ST st = group.getInstanceOf("a");
        String expected = " <bar> ";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultArgument() throws Exception
    {
        String templates = "method(name) ::= <<" +
            newline +
            "$stat(name)$" +
            newline +
            ">>" +
            newline +
            "stat(name,value=\"99\") ::= \"x=$value$; // $name$\"" +
            newline;
        writeFile(tmpdir, "group.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/group.stg", '$', '$');
        ST b = group.getInstanceOf("method");
        b.add("name", "foo");
        String expecting = "x=99; // foo";
        String result = b.render();
        assertEquals(expecting, result);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/46">antlr/stringtemplate4#46</a>.
     */
    @Test
    public void testDelimitersClause() throws Exception
    {
        String templates = "delimiters \"$\", \"$\"" +
            newline +
            "method(name) ::= <<" +
            newline +
            "$stat(name)$" +
            newline +
            ">>" +
            newline +
            "stat(name,value=\"99\") ::= \"x=$value$; // $name$\"" +
            newline;
        writeFile(tmpdir, "group.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/group.stg");
        ST b = group.getInstanceOf("method");
        b.add("name", "foo");
        String expecting = "x=99; // foo";
        String result = b.render();
        assertEquals(expecting, result);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/46">antlr/stringtemplate4#46</a>.
     */
    @Test
    public void testDelimitersClauseInGroupString() throws Exception
    {
        String templates = "delimiters \"$\", \"$\"" +
            newline +
            "method(name) ::= <<" +
            newline +
            "$stat(name)$" +
            newline +
            ">>" +
            newline +
            "stat(name,value=\"99\") ::= \"x=$value$; // $name$\"" +
            newline;
        STGroup group = new STGroupString(templates);
        ST b = group.getInstanceOf("method");
        b.add("name", "foo");
        String expecting = "x=99; // foo";
        String result = b.render();
        assertEquals(expecting, result);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/66">antlr/stringtemplate4#66</a>.
     */
    @Test
    public void testImportTemplatePreservesDelimiters()
    {
        String groupFile = "group GenerateHtml;" +
            newline +
            "import \"html.st\"" +
            newline +
            "entry() ::= <<" +
            newline +
            "$html()$" +
            newline +
            ">>" +
            newline;
        String htmlFile = "html() ::= <<" + newline + "<table style=\"stuff\">" + newline + ">>" + newline;

        String dir = getRandomDir();
        writeFile(dir, "GenerateHtml.stg", groupFile);
        writeFile(dir, "html.st", htmlFile);

        STGroup group = new STGroupFile(dir + "/GenerateHtml.stg", '$', '$');

        // test html template directly
        ST st = group.getInstanceOf("html");
        Assert.assertNotNull(st);
        String expected = "<table style=\"stuff\">";
        String result = st.render();
        assertEquals(expected, result);

        // test from entry template
        st = group.getInstanceOf("entry");
        Assert.assertNotNull(st);
        expected = "<table style=\"stuff\">";
        result = st.render();
        assertEquals(expected, result);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/66">antlr/stringtemplate4#66</a>.
     */
    @Test
    public void testImportGroupPreservesDelimiters()
    {
        String groupFile = "group GenerateHtml;" +
            newline +
            "import \"HtmlTemplates.stg\"" +
            newline +
            "entry() ::= <<" +
            newline +
            "$html()$" +
            newline +
            ">>" +
            newline;
        String htmlFile = "html() ::= <<" + newline + "<table style=\"stuff\">" + newline + ">>" + newline;

        String dir = getRandomDir();
        writeFile(dir, "GenerateHtml.stg", groupFile);
        writeFile(dir, "HtmlTemplates.stg", htmlFile);

        STGroup group = new STGroupFile(dir + "/GenerateHtml.stg", '$', '$');

        // test html template directly
        ST st = group.getInstanceOf("html");
        Assert.assertNotNull(st);
        String expected = "<table style=\"stuff\">";
        String result = st.render();
        assertEquals(expected, result);

        // test from entry template
        st = group.getInstanceOf("entry");
        Assert.assertNotNull(st);
        expected = "<table style=\"stuff\">";
        result = st.render();
        assertEquals(expected, result);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/66">antlr/stringtemplate4#66</a>.
     */
    @Test
    public void testDelimitersClauseOverridesConstructorDelimiters()
    {
        String groupFile = "group GenerateHtml;" +
            newline +
            "delimiters \"$\", \"$\"" +
            newline +
            "import \"html.st\"" +
            newline +
            "entry() ::= <<" +
            newline +
            "$html()$" +
            newline +
            ">>" +
            newline;
        String htmlFile = "html() ::= <<" + newline + "<table style=\"stuff\">" + newline + ">>" + newline;

        String dir = getRandomDir();
        writeFile(dir, "GenerateHtml.stg", groupFile);
        writeFile(dir, "html.st", htmlFile);

        STGroup group = new STGroupFile(dir + "/GenerateHtml.stg", '<', '>');

        // test html template directly
        ST st = group.getInstanceOf("html");
        Assert.assertNotNull(st);
        String expected = "<table style=\"stuff\">";
        String result = st.render();
        assertEquals(expected, result);

        // test from entry template
        st = group.getInstanceOf("entry");
        Assert.assertNotNull(st);
        expected = "<table style=\"stuff\">";
        result = st.render();
        assertEquals(expected, result);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/66">antlr/stringtemplate4#66</a>.
     */
    @Test
    public void testDelimitersClauseOverridesInheritedDelimiters()
    {
        String groupFile = "group GenerateHtml;" +
            newline +
            "delimiters \"<\", \">\"" +
            newline +
            "import \"HtmlTemplates.stg\"" +
            newline +
            "entry() ::= <<" +
            newline +
            "<html()>" +
            newline +
            ">>" +
            newline;
        String htmlFile = "delimiters \"$\", \"$\"" +
            newline +
            "html() ::= <<" +
            newline +
            "<table style=\"stuff\">" +
            newline +
            ">>" +
            newline;

        String dir = getRandomDir();
        writeFile(dir, "GenerateHtml.stg", groupFile);
        writeFile(dir, "HtmlTemplates.stg", htmlFile);

        STGroup group = new STGroupFile(dir + "/GenerateHtml.stg");

        // test html template directly
        ST st = group.getInstanceOf("html");
        Assert.assertNotNull(st);
        String expected = "<table style=\"stuff\">";
        String result = st.render();
        assertEquals(expected, result);

        // test from entry template
        st = group.getInstanceOf("entry");
        Assert.assertNotNull(st);
        expected = "<table style=\"stuff\">";
        result = st.render();
        assertEquals(expected, result);
    }
}
