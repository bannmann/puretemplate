package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Template;
import org.puretemplate.misc.ErrorBuffer;

class TestGroupSyntax extends BaseTest
{
    @Test
    void testSimpleGroup()
    {
        String templates = "t() ::= <<foo>>" + NEWLINE;
        String expected = "t() ::= <<" + NEWLINE + "foo" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testEscapedQuote()
    {
        // setTest(ranges) ::= "<ranges; separator=\"||\">"
        // has to unescape the strings.
        String templates = "setTest(ranges) ::= \"<ranges; separator=\\\"||\\\">\"" + NEWLINE;
        String expected = "setTest(ranges) ::= <<" + NEWLINE + "<ranges; separator=\"||\">" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testMultiTemplates()
    {
        String templates = "ta(x) ::= \"[<x>]\"" +
            NEWLINE +
            "duh() ::= <<hi there>>" +
            NEWLINE +
            "wow() ::= <<last>>" +
            NEWLINE;

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

        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testSetDefaultDelimiters()
    {
        String templates = "delimiters \"<\", \">\"" + NEWLINE + "ta(x) ::= \"[<x>]\"" + NEWLINE;
        ErrorBuffer errors = new ErrorBuffer();
        Template template = loadGroupFromString(templates, errors).getTemplate("ta");

        assertSingleArgRenderingResult("[hi]", template, "x", "hi");
        assertEquals("[]",
            errors.getErrors()
                .toString());
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/131">antlr/stringtemplate4#131</a>.
     */
    @Test
    void testSetDefaultDelimitersForStringBasedGroup()
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

        Template template = loader.getGroup()
            .fromString(templates)
            .withErrorListener(errors)
            .build()
            .getTemplate("chapter");

        assertSingleArgRenderingResult("chapter hi", template, "title", "hi");
        assertEquals("[]",
            errors.getErrors()
                .toString());
    }

    @Test
    void testSetNonDefaultDelimiters()
    {
        String templates = "delimiters \"%\", \"%\"" + NEWLINE + "ta(x) ::= \"[%x%]\"" + NEWLINE;
        Template template = loadGroupFromString(templates).getTemplate("ta");
        assertSingleArgRenderingResult("[hi]", template, "x", "hi");
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/84">antlr/stringtemplate4#84</a>.
     */
    @Test
    void testSetUnsupportedDelimiters_At()
    {
        String templates = "delimiters \"@\", \"@\"" + NEWLINE + "ta(x) ::= \"[<x>]\"" + NEWLINE;
        ErrorBuffer errors = new ErrorBuffer();
        Template template = loadGroupFromString(templates, errors).getTemplate("ta");

        assertSingleArgRenderingResult("[hi]", template, "x", "hi");
        assertEquals("[<string> 1:11: unsupported delimiter character: @, " +
                "<string> 1:16: unsupported delimiter character: @]",
            errors.getErrors()
                .toString());
    }

    @Test
    void testSingleTemplateWithArgs()
    {
        String templates = "t(a,b) ::= \"[<a>]\"" + NEWLINE;
        String expected = "t(a,b) ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testDefaultValues()
    {
        String templates = "t(a={def1},b=\"def2\") ::= \"[<a>]\"" + NEWLINE;
        String expected = "t(a={def1},b=\"def2\") ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testDefaultValues2()
    {
        String templates = "t(x, y, a={def1}, b=\"def2\") ::= \"[<a>]\"" + NEWLINE;
        String expected = "t(x,y,a={def1},b=\"def2\") ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testDefaultValueTemplateWithArg()
    {
        String templates = "t(a={x | 2*<x>}) ::= \"[<a>]\"" + NEWLINE;
        String expected = "t(a={x | 2*<x>}) ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testDefaultValueBehaviorTrue()
    {
        String templates = "t(a=true) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";
        assertNoArgRenderingResult("true+", loadGroupFromString(templates), "t");
    }

    @Test
    void testDefaultValueBehaviorFalse()
    {
        String templates = "t(a=false) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";
        assertNoArgRenderingResult("false-", loadGroupFromString(templates), "t");
    }

    @Test
    void testDefaultValueBehaviorEmptyTemplate()
    {
        String templates = "t(a={}) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";
        assertNoArgRenderingResult("+", loadGroupFromString(templates), "t");
    }

    @Test
    void testDefaultValueBehaviorEmptyList()
    {
        String templates = "t(a=[]) ::= <<\n" + "<a><if(a)>+<else>-<endif>\n" + ">>\n";
        assertNoArgRenderingResult("-", loadGroupFromString(templates), "t");
    }

    @Test
    void testNestedTemplateInGroupFile()
    {
        String templates = "t(a) ::= \"<a:{x | <x:{y | <y>}>}>\"" + NEWLINE;
        String expected = "t(a) ::= <<" + NEWLINE + "<a:{x | <x:{y | <y>}>}>" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testNestedDefaultValueTemplate()
    {
        String templates = "t(a={x | <x:{y|<y>}>}) ::= \"ick\"" + NEWLINE;
        String expected = "t(a={x | <x:{y|<y>}>}) ::= <<" + NEWLINE + "ick" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testNestedDefaultValueTemplateWithEscapes()
    {
        String templates = "t(a={x | \\< <x:{y|<y>\\}}>}) ::= \"[<a>]\"" + NEWLINE;
        String expected = "t(a={x | \\< <x:{y|<y>\\}}>}) ::= <<" + NEWLINE + "[<a>]" + NEWLINE + ">>" + NEWLINE;
        assertEquals(expected,
            loadGroupFromString(templates).diagnostics()
                .getDump());
    }

    @Test
    void testMessedUpTemplateDoesntCauseRuntimeError()
    {
        String templates = "main(p) ::= <<\n" + "<f(x=\"abc\")>\n" + ">>\n" + "\n" + "f() ::= <<\n" + "<x>\n" + ">>\n";
        ErrorBuffer errors = new ErrorBuffer();
        assertNoArgRenderingResult("", loadGroupFromString(templates, errors), "main");
        assertEquals("[context [/main] 1:1 attribute x isn't defined," +
                " context [/main] 1:1 passed 1 arg(s) to template /f with 0 declared arg(s)," +
                " context [/main /f] 1:1 attribute x isn't defined]",
            errors.getErrors()
                .toString());
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/138">antlr/stringtemplate4#138</a>.
     */
    @Test
    void testIndentedComment()
    {
        String templates = "t() ::= <<" + NEWLINE + "  <! a comment !>" + NEWLINE + ">>" + NEWLINE;
        ErrorBuffer errors = new ErrorBuffer();

        assertNoArgRenderingResult("", loadGroupFromString(templates, errors), "t");
        assertEquals(Collections.emptyList(), errors.getErrors());
    }
}
