package org.puretemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.puretemplate.misc.ErrorBuffer;

public class TestGroupSyntax extends BaseTest
{
    @Test
    public void testSimpleGroup()
    {
        String templates = "t() ::= <<foo>>" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        assertEquals("t() ::= <<" + NEWLINE + "foo" + NEWLINE + ">>" + NEWLINE, group.show());
    }

    @Test
    public void testEscapedQuote()
    {
        // setTest(ranges) ::= "<ranges; separator=\"||\">"
        // has to unescape the strings.
        String templates = "setTest(ranges) ::= \"<ranges; separator=\\\"||\\\">\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        String expected = "setTest(ranges) ::= <<" + NEWLINE + "<ranges; separator=\"||\">" + NEWLINE + ">>" + NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testMultiTemplates()
    {
        String templates = "ta(x) ::= \"[<x>]\"" +
            NEWLINE +
            "duh() ::= <<hi there>>" +
            NEWLINE +
            "wow() ::= <<last>>" +
            NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        String expected = "ta(x) ::= <<" +
            NEWLINE +
            "[<x>]" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "duh() ::= <<" +
            NEWLINE +
            "hi there" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "wow() ::= <<" +
            NEWLINE +
            "last" +
            NEWLINE +
            ">>" +
            NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testSetDefaultDelimiters() throws IOException
    {
        String templates = "delimiters \"<\", \">\"" + NEWLINE + "ta(x) ::= \"[<x>]\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("ta");
        st.add("x", "hi");
        assertRenderingResult("[hi]", st);

        assertEquals("[]",
            errors.getErrors()
                .toString());
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/131">antlr/stringtemplate4#131</a>.
     */
    @Test
    public void testSetDefaultDelimitersForStringBasedGroup()
    {
        String templates = "delimiters \"<\", \">\"" +
            NEWLINE +
            "chapter(title) ::= <<" +
            NEWLINE +
            "chapter <title>" +
            NEWLINE +
            ">>" +
            NEWLINE;

        ErrorBuffer errors = new ErrorBuffer();

        Context context = loader.getGroup()
            .fromString(templates)
            .withErrorListener(errors)
            .build()
            .getTemplate("chapter")
            .createContext()
            .add("title", "hi");
        assertRenderingResult("chapter hi", context);

        assertEquals("[]",
            errors.getErrors()
                .toString());
    }

    @Test
    public void testSetNonDefaultDelimiters()
    {
        String templates = "delimiters \"%\", \"%\"" + NEWLINE + "ta(x) ::= \"[%x%]\"" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("ta")
            .createContext()
            .add("x", "hi");

        assertRenderingResult("[hi]", context);
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/84">antlr/stringtemplate4#84</a>.
     */
    @Test
    public void testSetUnsupportedDelimiters_At() throws IOException
    {
        String templates = "delimiters \"@\", \"@\"" + NEWLINE + "ta(x) ::= \"[<x>]\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("ta");
        st.add("x", "hi");
        assertRenderingResult("[hi]", st);

        String expectedErrors = "[t.stg 1:11: unsupported delimiter character: @, " +
            "t.stg 1:16: unsupported delimiter character: @]";
        String resultErrors = errors.getErrors()
            .toString();
        assertEquals(expectedErrors, resultErrors);
    }

    @Test
    public void testSingleTemplateWithArgs()
    {
        String templates = "t(a,b) ::= \"[<a>]\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        assertEquals("t(a,b) ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE, group.show());
    }

    @Test
    public void testDefaultValues()
    {
        String templates = "t(a={def1},b=\"def2\") ::= \"[<a>]\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        assertEquals("t(a={def1},b=\"def2\") ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE, group.show());
    }

    @Test
    public void testDefaultValues2()
    {
        String templates = "t(x, y, a={def1}, b=\"def2\") ::= \"[<a>]\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        String expected = "t(x,y,a={def1},b=\"def2\") ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testDefaultValueTemplateWithArg()
    {
        String templates = "t(a={x | 2*<x>}) ::= \"[<a>]\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        assertEquals("t(a={x | 2*<x>}) ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE, group.show());
    }

    @Test
    public void testDefaultValueBehaviorTrue() throws IOException
    {
        String templates = "t(a=true) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("true+", st);
    }

    @Test
    public void testDefaultValueBehaviorFalse() throws IOException
    {
        String templates = "t(a=false) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("false-", st);
    }

    @Test
    public void testDefaultValueBehaviorEmptyTemplate() throws IOException
    {
        String templates = "t(a={}) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("+", st);
    }

    @Test
    public void testDefaultValueBehaviorEmptyList() throws IOException
    {
        String templates = "t(a=[]) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("-", st);
    }

    @Test
    public void testNestedTemplateInGroupFile()
    {
        String templates = "t(a) ::= \"<a:{x | <x:{y | <y>}>}>\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        assertEquals("t(a) ::= <<" + NEWLINE + "<a:{x | <x:{y | <y>}>}>" + NEWLINE + ">>" + NEWLINE, group.show());
    }

    @Test
    public void testNestedDefaultValueTemplate()
    {
        String templates = "t(a={x | <x:{y|<y>}>}) ::= \"ick\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.load();
        assertEquals("t(a={x | <x:{y|<y>}>}) ::= <<" + NEWLINE + "ick" + NEWLINE + ">>" + NEWLINE, group.show());
    }

    @Test
    public void testNestedDefaultValueTemplateWithEscapes()
    {
        String templates = "t(a={x | \\< <x:{y|<y>\\}}>}) ::= \"[<a>]\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        String expected = "t(a={x | \\< <x:{y|<y>\\}}>}) ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE;
        String result = group.show();
        assertEquals(expected, result);
    }

    @Test
    public void testMessedUpTemplateDoesntCauseRuntimeError()
    {
        String templates = "main(p) ::= <<\n" + "<f(x=\"abc\")>\n" + ">>\n" + "\n" + "f() ::= <<\n" + "<x>\n" + ">>\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("main");
        st.render();

        String expected = "[context [/main] 1:1 attribute x isn't defined," +
            " context [/main] 1:1 passed 1 arg(s) to template /f with 0 declared arg(s)," +
            " context [/main /f] 1:1 attribute x isn't defined]";
        String result = errors.getErrors()
            .toString();
        assertEquals(expected, result);
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/138">antlr/stringtemplate4#138</a>.
     */
    @Test
    public void testIndentedComment() throws IOException
    {
        String templates = "t() ::= <<" + NEWLINE + "  <! a comment !>" + NEWLINE + ">>" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST template = group.getInstanceOf("t");

        assertEquals("[]",
            errors.getErrors()
                .toString());
        assertNotNull(template);

        assertRenderingResult("", template);

        assertEquals("[]",
            errors.getErrors()
                .toString());
    }
}
