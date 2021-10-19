package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

class TestWhitespace extends BaseTest
{
    @Test
    void testTrimmedSubtemplates() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n | <n>}>!");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        assertRenderingResult("TerTomSumana!", st);
    }

    @Test
    void testTrimmedNewlinesBeforeAfterInTemplate()
    {
        Group group = loader.getGroup()
            .fromString("a(x) ::= <<" + NEWLINE + "foo" + NEWLINE + ">>" + NEWLINE)
            .build();
        assertEquals("foo", renderGroupTemplate(group, "a"));
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/93">antlr/stringtemplate4#93</a>.
     */
    @Test
    void testNoTrimmedNewlinesBeforeAfterInCodedTemplate()
    {
        Context context = makeTemplateContext(NEWLINE + "foo" + NEWLINE);
        assertRenderingResult(NEWLINE + "foo" + NEWLINE, context);
    }

    @Test
    void testDontTrimJustSpaceBeforeAfterInTemplate()
    {
        Group group = loader.getGroup()
            .fromString("a(x) ::= << foo >>\n")
            .build();
        assertEquals(" foo ", renderGroupTemplate(group, "a"));
    }

    @Test
    void testTrimmedSubtemplatesNoArgs() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "[<foo({ foo })>]");
        group.defineTemplate("foo", "x", "<x>");
        ST st = group.getInstanceOf("test");
        assertRenderingResult("[ foo ]", st);
    }

    @Test
    void testTrimmedSubtemplatesArgs() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{x|  foo }>");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        assertRenderingResult(" foo  foo  foo ", st);
    }

    @Test
    void testTrimJustOneWSInSubtemplates() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n |  <n> }>!");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        assertRenderingResult(" Ter  Tom  Sumana !", st);
    }

    @Test
    void testTrimNewlineInSubtemplates() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n |\n" + "<n>}>!");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        assertRenderingResult("TerTomSumana!", st);
    }

    @Test
    void testLeaveNewlineOnEndInSubtemplates() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n |\n" + "<n>\n" + "}>!");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        assertRenderingResult("Ter" + NEWLINE + "Tom" + NEWLINE + "Sumana" + NEWLINE + "!", st);
    }

    @Test
    void testEmptyExprAsFirstLineGetsNoOutput()
    {
        assertNoArgRenderingResult("end" + NEWLINE, "<users>\n" + "end\n");
    }

    @Test
    void testEmptyLineWithIndent()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + NEWLINE + "end" + NEWLINE, "begin\n" + "    \n" + "end\n");
    }

    @Test
    void testEmptyLine()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + NEWLINE + "end" + NEWLINE, "begin\n" + "\n" + "end\n");
    }

    @Test
    void testSizeZeroOnLineByItselfGetsNoOutput()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "<name>\n" + "<users>\n" + "<users>\n" + "end\n");
    }

    @Test
    void testSizeZeroOnLineWithIndentGetsNoOutput()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "  <name>\n" + "   <users>\n" + "   <users>\n" + "end\n");
    }

    @Test
    void testSizeZeroOnLineWithMultipleExpr()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "  <name>\n" + "   <users><users>\n" + "end\n");
    }

    @Test
    void testIFExpr()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE, "begin\n" + "<if(x)><endif>\n" + "end\n");
    }

    @Test
    void testIndentedIFExpr()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE, "begin\n" + "    <if(x)><endif>\n" + "end\n");
    }

    @Test
    void testIFElseExprOnSingleLine()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "<if(users)><else><endif>\n" + "end\n");
    }

    @Test
    void testIFOnMultipleLines()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "bar" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "<if(users)>\n" + "foo\n" + "<else>\n" + "bar\n" + "<endif>\n" + "end\n");
    }

    @Test
    void testEndifNotOnLineAlone()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "  bar" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "  <if(users)>\n" + "  foo\n" + "  <else>\n" + "  bar\n" + "  <endif>end\n");
    }

    @Test
    void testElseIFOnMultipleLines()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "<if(a)>\n" + "foo\n" + "<elseif(b)>\n" + "bar\n" + "<endif>\n" + "end\n");
    }

    @Test
    void testElseIFOnMultipleLines2()
    {
        Context context = makeTemplateContext("begin\n" +
            "<if(a)>\n" +
            "foo\n" +
            "<elseif(b)>\n" +
            "bar\n" +
            "<endif>\n" +
            "end\n").add("b", true);
        assertRenderingResult("begin" + NEWLINE + "bar" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    void testElseIFOnMultipleLines3()
    {
        Context context = makeTemplateContext("begin\n" +
            "  <if(a)>\n" +
            "  foo\n" +
            "  <elseif(b)>\n" +
            "  bar\n" +
            "  <endif>\n" +
            "end\n").add("a", true);
        assertRenderingResult("begin" + NEWLINE + "  foo" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    void testNestedIFOnMultipleLines()
    {
        Context context = makeTemplateContext("begin\n" +
            "<if(x)>\n" +
            "<if(y)>\n" +
            "foo\n" +
            "<else>\n" +
            "bar\n" +
            "<endif>\n" +
            "<endif>\n" +
            "end\n").add("x", "x");
        assertRenderingResult("begin" + NEWLINE + "bar" + NEWLINE + "end" + NEWLINE, context);
    }

    @Test
    void testLineBreak() throws IOException
    {
        ST st = new ST("Foo <\\\\>" + NEWLINE + "  \t  bar" + NEWLINE);
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo bar\n";     // expect \n in output
        assertEquals(expecting, result);
    }

    @Test
    void testLineBreak2() throws IOException
    {
        ST st = new ST("Foo <\\\\>       " + NEWLINE + "  \t  bar" + NEWLINE);
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo bar\n";
        assertEquals(expecting, result);
    }

    @Test
    void testLineBreakNoWhiteSpace() throws IOException
    {
        ST st = new ST("Foo <\\\\>" + NEWLINE + "bar\n");
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo bar\n";
        assertEquals(expecting, result);
    }

    @Test
    void testNewlineNormalizationInTemplateString() throws IOException
    {
        ST st = new ST("Foo\r\n" + "Bar\n");
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo\nBar\n";     // expect \n in output
        assertEquals(expecting, result);
    }

    @Test
    void testNewlineNormalizationInTemplateStringPC() throws IOException
    {
        ST st = new ST("Foo\r\n" + "Bar\n");
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\r\n")); // force \r\n as newline
        String result = sw.toString();
        String expecting = "Foo\r\nBar\r\n";     // expect \r\n in output
        assertEquals(expecting, result);
    }

    @Test
    void testNewlineNormalizationInAttribute() throws IOException
    {
        ST st = new ST("Foo\r\n" + "<name>\n");
        st.add("name", "a\nb\r\nc");
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo\na\nb\nc\n";     // expect \n in output
        assertEquals(expecting, result);
    }

    @Test
    void testCommentOnlyLineGivesNoOutput()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE, "begin\n" + "<! ignore !>\n" + "end\n");
    }

    @Test
    void testCommentOnlyLineGivesNoOutput2()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE, "begin\n" + "    <! ignore !>\n" + "end\n");
    }
}
