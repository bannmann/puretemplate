package com.github.bannmann.puretemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import com.github.bannmann.puretemplate.misc.ErrorBuffer;
import com.github.bannmann.puretemplate.misc.Misc;

public class TestGroupSyntax extends BaseTest
{
    @Test
    public void testSimpleGroup() throws Exception
    {
        String templates = "t() ::= <<foo>>" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "t() ::= <<" + Misc.NEWLINE + "foo" + Misc.NEWLINE + ">>" + Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testEscapedQuote() throws Exception
    {
        // setTest(ranges) ::= "<ranges; separator=\"||\">"
        // has to unescape the strings.
        String templates = "setTest(ranges) ::= \"<ranges; separator=\\\"||\\\">\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "setTest(ranges) ::= <<" +
            Misc.NEWLINE +
            "<ranges; separator=\"||\">" +
            Misc.NEWLINE +
            ">>" +
            Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testMultiTemplates() throws Exception
    {
        String templates = "ta(x) ::= \"[<x>]\"" +
            Misc.NEWLINE +
            "duh() ::= <<hi there>>" +
            Misc.NEWLINE +
            "wow() ::= <<last>>" +
            Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "ta(x) ::= <<" +
            Misc.NEWLINE +
            "[<x>]" +
            Misc.NEWLINE +
            ">>" +
            Misc.NEWLINE +
            "duh() ::= <<" +
            Misc.NEWLINE +
            "hi there" +
            Misc.NEWLINE +
            ">>" +
            Misc.NEWLINE +
            "wow() ::= <<" +
            Misc.NEWLINE +
            "last" +
            Misc.NEWLINE +
            ">>" +
            Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testSetDefaultDelimiters() throws Exception
    {
        String templates = "delimiters \"<\", \">\"" + Misc.NEWLINE + "ta(x) ::= \"[<x>]\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("ta");
        st.add("x", "hi");
        String expected = "[hi]";
        String result = st.render();
        assertEquals(expected, result);

        assertEquals("[]", errors.errors.toString());
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/131">antlr/stringtemplate4#131</a>.
     */
    @Test
    public void testSetDefaultDelimiters_STGroupString() throws Exception
    {
        String templates = "delimiters \"<\", \">\"" +
            Misc.NEWLINE +
            "chapter(title) ::= <<" +
            Misc.NEWLINE +
            "chapter <title>" +
            Misc.NEWLINE +
            ">>" +
            Misc.NEWLINE;

        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new STGroupString(templates);
        group.setListener(errors);
        ST st = group.getInstanceOf("chapter");
        st.add("title", "hi");
        String expected = "chapter hi";
        String result = st.render();
        assertEquals(expected, result);

        assertEquals("[]", errors.errors.toString());
    }

    @Test
    public void testSetNonDefaultDelimiters() throws Exception
    {
        String templates = "delimiters \"%\", \"%\"" + Misc.NEWLINE + "ta(x) ::= \"[%x%]\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        ST st = group.getInstanceOf("ta");
        st.add("x", "hi");
        String expected = "[hi]";
        String result = st.render();
        assertEquals(expected, result);
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/84">antlr/stringtemplate4#84</a>.
     */
    @Test
    public void testSetUnsupportedDelimiters_At() throws Exception
    {
        String templates = "delimiters \"@\", \"@\"" + Misc.NEWLINE + "ta(x) ::= \"[<x>]\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("ta");
        st.add("x", "hi");
        String expected = "[hi]";
        String result = st.render();
        assertEquals(expected, result);

        String expectedErrors = "[t.stg 1:11: unsupported delimiter character: @, " +
            "t.stg 1:16: unsupported delimiter character: @]";
        String resultErrors = errors.errors.toString();
        assertEquals(expectedErrors, resultErrors);
    }

    @Test
    public void testSingleTemplateWithArgs() throws Exception
    {
        String templates = "t(a,b) ::= \"[<a>]\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "t(a,b) ::= <<" + Misc.NEWLINE + "[<a>]" + Misc.NEWLINE + ">>" + Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultValues() throws Exception
    {
        String templates = "t(a={def1},b=\"def2\") ::= \"[<a>]\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "t(a={def1},b=\"def2\") ::= <<" + Misc.NEWLINE + "[<a>]" + Misc.NEWLINE + ">>" + Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultValues2() throws Exception
    {
        String templates = "t(x, y, a={def1}, b=\"def2\") ::= \"[<a>]\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "t(x,y,a={def1},b=\"def2\") ::= <<" +
            Misc.NEWLINE +
            "[<a>]" +
            Misc.NEWLINE +
            ">>" +
            Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultValueTemplateWithArg() throws Exception
    {
        String templates = "t(a={x | 2*<x>}) ::= \"[<a>]\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "t(a={x | 2*<x>}) ::= <<" + Misc.NEWLINE + "[<a>]" + Misc.NEWLINE + ">>" + Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultValueBehaviorTrue() throws Exception
    {
        String templates = "t(a=true) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        String expected = "true+";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultValueBehaviorFalse() throws Exception
    {
        String templates = "t(a=false) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        String expected = "false-";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultValueBehaviorEmptyTemplate() throws Exception
    {
        String templates = "t(a={}) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        String expected = "+";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultValueBehaviorEmptyList() throws Exception
    {
        String templates = "t(a=[]) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        String expected = "-";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testNestedTemplateInGroupFile() throws Exception
    {
        String templates = "t(a) ::= \"<a:{x | <x:{y | <y>}>}>\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "t(a) ::= <<" + newline + "<a:{x | <x:{y | <y>}>}>" + newline + ">>" + Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testNestedDefaultValueTemplate() throws Exception
    {
        String templates = "t(a={x | <x:{y|<y>}>}) ::= \"ick\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.load();
        String expected = "t(a={x | <x:{y|<y>}>}) ::= <<" + newline + "ick" + newline + ">>" + Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testNestedDefaultValueTemplateWithEscapes() throws Exception
    {
        String templates = "t(a={x | \\< <x:{y|<y>\\}}>}) ::= \"[<a>]\"" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        String expected = "t(a={x | \\< <x:{y|<y>\\}}>}) ::= <<" +
            Misc.NEWLINE +
            "[<a>]" +
            Misc.NEWLINE +
            ">>" +
            Misc.NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testMessedUpTemplateDoesntCauseRuntimeError() throws Exception
    {
        String templates = "main(p) ::= <<\n" + "<f(x=\"abc\")>\n" + ">>\n" + "\n" + "f() ::= <<\n" + "<x>\n" + ">>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("main");
        st.render();

        String expected = "[context [/main] 1:1 attribute x isn't defined," +
            " context [/main] 1:1 passed 1 arg(s) to template /f with 0 declared arg(s)," +
            " context [/main /f] 1:1 attribute x isn't defined]";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/138">antlr/stringtemplate4#138</a>.
     */
    @Test
    public void testIndentedComment() throws Exception
    {
        String templates = "t() ::= <<" + Misc.NEWLINE + "  <! a comment !>" + Misc.NEWLINE + ">>" + Misc.NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST template = group.getInstanceOf("t");

        assertEquals("[]", errors.errors.toString());
        assertNotNull(template);

        String expected = "";
        String result = template.render();
        assertEquals(expected, result);

        assertEquals("[]", errors.errors.toString());
    }
}
