package org.puretemplate;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

public class TestWhitespace extends BaseTest
{
    @Test
    public void testTrimmedSubtemplates() throws IOException
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
    public void testTrimmedNewlinesBeforeAfterInTemplate()
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
    public void testNoTrimmedNewlinesBeforeAfterInCodedTemplate()
    {
        Context context = makeTemplateContext(NEWLINE + "foo" + NEWLINE);
        assertRenderingResult(NEWLINE + "foo" + NEWLINE, context);
    }

    @Test
    public void testDontTrimJustSpaceBeforeAfterInTemplate()
    {
        Group group = loader.getGroup()
            .fromString("a(x) ::= << foo >>\n")
            .build();
        assertEquals(" foo ", renderGroupTemplate(group, "a"));
    }

    @Test
    public void testTrimmedSubtemplatesNoArgs() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "[<foo({ foo })>]");
        group.defineTemplate("foo", "x", "<x>");
        ST st = group.getInstanceOf("test");
        assertRenderingResult("[ foo ]", st);
    }

    @Test
    public void testTrimmedSubtemplatesArgs() throws IOException
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
    public void testTrimJustOneWSInSubtemplates() throws IOException
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
    public void testTrimNewlineInSubtemplates() throws IOException
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
    public void testLeaveNewlineOnEndInSubtemplates() throws IOException
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
    public void testEmptyExprAsFirstLineGetsNoOutput()
    {
        assertNoArgRenderingResult("end" + NEWLINE, "<users>\n" + "end\n");
    }

    @Test
    public void testEmptyLineWithIndent()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + NEWLINE + "end" + NEWLINE, "begin\n" + "    \n" + "end\n");
    }

    @Test
    public void testEmptyLine()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + NEWLINE + "end" + NEWLINE, "begin\n" + "\n" + "end\n");
    }

    @Test
    public void testSizeZeroOnLineByItselfGetsNoOutput()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "<name>\n" + "<users>\n" + "<users>\n" + "end\n");
    }

    @Test
    public void testSizeZeroOnLineWithIndentGetsNoOutput()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "  <name>\n" + "   <users>\n" + "   <users>\n" + "end\n");
    }

    @Test
    public void testSizeZeroOnLineWithMultipleExpr()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "  <name>\n" + "   <users><users>\n" + "end\n");
    }

    @Test
    public void testIFExpr()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE, "begin\n" + "<if(x)><endif>\n" + "end\n");
    }

    @Test
    public void testIndentedIFExpr()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE, "begin\n" + "    <if(x)><endif>\n" + "end\n");
    }

    @Test
    public void testIFElseExprOnSingleLine()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "<if(users)><else><endif>\n" + "end\n");
    }

    @Test
    public void testIFOnMultipleLines()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "bar" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "<if(users)>\n" + "foo\n" + "<else>\n" + "bar\n" + "<endif>\n" + "end\n");
    }

    @Test
    public void testEndifNotOnLineAlone()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "  bar" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "  <if(users)>\n" + "  foo\n" + "  <else>\n" + "  bar\n" + "  <endif>end\n");
    }

    @Test
    public void testElseIFOnMultipleLines()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE,
            "begin\n" + "<if(a)>\n" + "foo\n" + "<elseif(b)>\n" + "bar\n" + "<endif>\n" + "end\n");
    }

    @Test
    public void testElseIFOnMultipleLines2()
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
    public void testElseIFOnMultipleLines3()
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
    public void testNestedIFOnMultipleLines()
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
    public void testLineBreak() throws IOException
    {
        ST st = new ST("Foo <\\\\>" + NEWLINE + "  \t  bar" + NEWLINE);
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo bar\n";     // expect \n in output
        assertEquals(expecting, result);
    }

    @Test
    public void testLineBreak2() throws IOException
    {
        ST st = new ST("Foo <\\\\>       " + NEWLINE + "  \t  bar" + NEWLINE);
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo bar\n";
        assertEquals(expecting, result);
    }

    @Test
    public void testLineBreakNoWhiteSpace() throws IOException
    {
        ST st = new ST("Foo <\\\\>" + NEWLINE + "bar\n");
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo bar\n";
        assertEquals(expecting, result);
    }

    @Test
    public void testNewlineNormalizationInTemplateString() throws IOException
    {
        ST st = new ST("Foo\r\n" + "Bar\n");
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo\nBar\n";     // expect \n in output
        assertEquals(expecting, result);
    }

    @Test
    public void testNewlineNormalizationInTemplateStringPC() throws IOException
    {
        ST st = new ST("Foo\r\n" + "Bar\n");
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\r\n")); // force \r\n as newline
        String result = sw.toString();
        String expecting = "Foo\r\nBar\r\n";     // expect \r\n in output
        assertEquals(expecting, result);
    }

    @Test
    public void testNewlineNormalizationInAttribute() throws IOException
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
    public void testCommentOnlyLineGivesNoOutput()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE, "begin\n" + "<! ignore !>\n" + "end\n");
    }

    @Test
    public void testCommentOnlyLineGivesNoOutput2()
    {
        assertNoArgRenderingResult("begin" + NEWLINE + "end" + NEWLINE, "begin\n" + "    <! ignore !>\n" + "end\n");
    }
}
