package com.example;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.model.Aggregate;

public class TestLineWrap extends BaseTest
{
    private static final int[] MANY_INTEGERS = {
        3,
        9,
        20,
        2,
        1,
        4,
        6,
        32,
        5,
        6,
        77,
        888,
        2,
        1,
        6,
        32,
        5,
        6,
        77,
        4,
        9,
        20,
        2,
        1,
        4,
        63,
        9,
        20,
        2,
        1,
        4,
        6,
        32,
        5,
        6,
        77,
        6,
        32,
        5,
        6,
        77,
        3,
        9,
        20,
        2,
        1,
        4,
        6,
        32,
        5,
        6,
        77,
        888,
        1,
        6,
        32,
        5
    };

    @Test
    public void testLineWrap() throws IOException
    {
        String templates = "array(values) ::= <<int[] a = { <values; wrap=\"\\n\", separator=\",\"> };>>" + NEWLINE;
        Group group = loadGroupViaDisk(templates);

        Context context = group.getTemplate("array")
            .createContext()
            .add("values", MANY_INTEGERS);

        String expecting = "int[] a = { 3,9,20,2,1,4,6,32,5,6,77,888," +
            NEWLINE +
            "2,1,6,32,5,6,77,4,9,20,2,1,4,63,9,20,2,1," +
            NEWLINE +
            "4,6,32,5,6,77,6,32,5,6,77,3,9,20,2,1,4,6," +
            NEWLINE +
            "32,5,6,77,888,1,6,32,5 };";
        assertWrappedRenderingResult(expecting, 40, context);
    }

    @Test
    public void testLineWrapAnchored() throws IOException
    {
        String templates = "array(values) ::= <<int[] a = { <values; anchor, wrap, separator=\",\"> };>>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("array")
            .createContext()
            .add("values", MANY_INTEGERS);

        String expecting = "int[] a = { 3,9,20,2,1,4,6,32,5,6,77,888," +
            NEWLINE +
            "            2,1,6,32,5,6,77,4,9,20,2,1,4," +
            NEWLINE +
            "            63,9,20,2,1,4,6,32,5,6,77,6," +
            NEWLINE +
            "            32,5,6,77,3,9,20,2,1,4,6,32," +
            NEWLINE +
            "            5,6,77,888,1,6,32,5 };";
        assertWrappedRenderingResult(expecting, 40, context);
    }

    @Test
    public void testSubtemplatesAnchorToo()
    {
        Group group = loadGroupFromString("array(values) ::= <<{ <values; anchor, separator=\", \"> }>>" + NEWLINE);

        Context x = loader.getTemplate()
            .fromString("<\\n>{ <stuff; anchor, separator=\",\\n\"> }<\\n>")
            .attachedToGroup(group)
            .build()
            .createContext()
            .add("stuff", "1")
            .add("stuff", "2")
            .add("stuff", "3");

        Context array = group.getTemplate("array")
            .createContext()
            .add("values", List.of("a", x, "b"));

        String expecting = "{ a, " +
            NEWLINE +
            "  { 1," +
            NEWLINE +
            "    2," +
            NEWLINE +
            "    3 }" +
            NEWLINE +
            "  , b }";
        assertWrappedRenderingResult(expecting, 40, array);
    }

    @Test
    public void testFortranLineWrap() throws IOException
    {
        String templates = "func(args) ::= <<       FUNCTION line( <args; wrap=\"\\n      c\", separator=\",\"> )>>" +
            NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("func")
            .createContext()
            .add("args", new String[]{ "a", "b", "c", "d", "e", "f" });

        String expecting = "       FUNCTION line( a,b,c,d," + NEWLINE + "      ce,f )";
        assertWrappedRenderingResult(expecting, 30, context);
    }

    @Test
    public void testLineWrapWithDiffAnchor() throws IOException
    {
        String templates = "array(values) ::= <<int[] a = { <{1,9,2,<values; wrap, separator=\",\">}; anchor> };>>" +
            NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("array")
            .createContext()
            .add("values", MANY_INTEGERS);

        String expecting = "int[] a = { 1,9,2,3,9,20,2,1,4," +
            NEWLINE +
            "            6,32,5,6,77,888,2," +
            NEWLINE +
            "            1,6,32,5,6,77,4,9," +
            NEWLINE +
            "            20,2,1,4,63,9,20,2," +
            NEWLINE +
            "            1,4,6,32,5,6,77,6," +
            NEWLINE +
            "            32,5,6,77,3,9,20,2," +
            NEWLINE +
            "            1,4,6,32,5,6,77,888," +
            NEWLINE +
            "            1,6,32,5 };";
        assertWrappedRenderingResult(expecting, 30, context);
    }

    @Test
    public void testLineWrapEdgeCase() throws IOException
    {
        String templates = "duh(chars) ::= \"<chars; wrap={<\\n>}>\"" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("duh")
            .createContext()
            .add("chars", new String[]{ "a", "b", "c", "d", "e" });

        // lineWidth==3 implies that we can have 3 characters at most
        String expecting = "abc" + NEWLINE + "de";
        assertWrappedRenderingResult(expecting, 3, context);

        Aggregate value = Aggregate.build()
            .properties("a", "b")
            .withValues(1, true);
    }

    @Test
    public void testLineWrapLastCharIsNewline() throws IOException
    {
        String templates = "duh(chars) ::= <<" + NEWLINE + "<chars; wrap=\"\\n\"\\>" + NEWLINE + ">>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("duh")
            .createContext()
            .add("chars", new String[]{ "a", "b", NEWLINE, "d", "e" });

        // don't do \n if it's last element anyway
        String expecting = "ab" + NEWLINE + "de";
        assertWrappedRenderingResult(expecting, 3, context);
    }

    @Test
    public void testLineWrapCharAfterWrapIsNewline() throws IOException
    {
        String templates = "duh(chars) ::= <<" + NEWLINE + "<chars; wrap=\"\\n\"\\>" + NEWLINE + ">>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("duh")
            .createContext()
            .add("chars", new String[]{ "a", "b", "c", NEWLINE, "d", "e" });

        /*
         * Once we wrap, we must dump chars as we see them. A newline right after a wrap is just an "unfortunate" event.
         * People will expect a newline if it's in the data.
         */
        String expecting = "abc" + NEWLINE + "" + NEWLINE + "de";
        assertWrappedRenderingResult(expecting, 3, context);
    }

    @Test
    public void testLineWrapForList() throws IOException
    {
        String templates = "duh(data) ::= <<!<data; wrap>!>>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("duh")
            .createContext()
            .add("data", new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        String expecting = "!123" + NEWLINE + "4567" + NEWLINE + "89!";
        assertWrappedRenderingResult(expecting, 4, context);
    }

    @Test
    public void testLineWrapForAnonTemplate() throws IOException
    {
        String templates = "duh(data) ::= <<!<data:{v|[<v>]}; wrap>!>>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("duh")
            .createContext()
            .add("data", new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        // width=9 is reached with the '3' char; but we don't break until after the ']'
        assertWrappedRenderingResult("![1][2][3]" + NEWLINE + "[4][5][6]" + NEWLINE + "[7][8][9]!", 9, context);
    }

    @Test
    public void testLineWrapForAnonTemplateAnchored() throws IOException
    {
        String templates = "duh(data) ::= <<!<data:{v|[<v>]}; anchor, wrap>!>>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("duh")
            .createContext()
            .add("data", new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        assertWrappedRenderingResult("![1][2][3]" + NEWLINE + " [4][5][6]" + NEWLINE + " [7][8][9]!", 9, context);
    }

    @Test
    public void testLineWrapForAnonTemplateComplicatedWrap() throws IOException
    {
        String templates = "top(s) ::= <<  <s>.>>" + "str(data) ::= <<!<data:{v|[<v>]}; wrap=\"!+\\n!\">!>>" + NEWLINE;
        Group group = loadGroupViaDisk(templates);

        Context s = group.getTemplate("str")
            .createContext()
            .add("data", new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        Context t = group.getTemplate("top")
            .createContext()
            .add("s", s);

        String expecting = "  ![1][2]!+" +
            NEWLINE +
            "  ![3][4]!+" +
            NEWLINE +
            "  ![5][6]!+" +
            NEWLINE +
            "  ![7][8]!+" +
            NEWLINE +
            "  ![9]!.";

        assertWrappedRenderingResult(expecting, 9, t);
    }

    @Test
    public void testIndentBeyondLineWidth() throws IOException
    {
        String templates = "duh(chars) ::= <<" + NEWLINE + "    <chars; wrap=\"\\n\">" + NEWLINE + ">>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("duh")
            .createContext()
            .add("chars", new String[]{ "a", "b", "c", "d", "e" });

        assertWrappedRenderingResult("    a" +
            NEWLINE +
            "    b" +
            NEWLINE +
            "    c" +
            NEWLINE +
            "    d" +
            NEWLINE +
            "    e", 2, context);
    }

    @Test
    public void testIndentedExpr() throws IOException
    {
        String templates = "duh(chars) ::= <<" + NEWLINE + "    <chars; wrap=\"\\n\">" + NEWLINE + ">>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("duh")
            .createContext()
            .add("chars", new String[]{ "a", "b", "c", "d", "e" });

        // width=4 spaces + 2 char.
        assertWrappedRenderingResult("    ab" + NEWLINE + "    cd" + NEWLINE + "    e", 6, context);
    }

    @Test
    public void testNestedIndentedExpr() throws IOException
    {
        String templates = "top(d) ::= <<  <d>!>>" +
            NEWLINE +
            "duh(chars) ::= <<" +
            NEWLINE +
            "  <chars; wrap=\"\\n\">" +
            NEWLINE +
            ">>" +
            NEWLINE;
        Group group = loadGroupViaDisk(templates);

        Context duh = group.getTemplate("duh")
            .createContext()
            .add("chars", new String[]{ "a", "b", "c", "d", "e" });

        Context top = group.getTemplate("top")
            .createContext()
            .add("d", duh);

        // width=4 spaces + 2 char.
        assertWrappedRenderingResult("    ab" + NEWLINE + "    cd" + NEWLINE + "    e!", 6, top);
    }

    @Test
    public void testNestedWithIndentAndTrackStartOfExpr() throws IOException
    {
        String templates = "top(d) ::= <<  <d>!>>" +
            NEWLINE +
            "duh(chars) ::= <<" +
            NEWLINE +
            "x: <chars; anchor, wrap=\"\\n\">" +
            NEWLINE +
            ">>" +
            NEWLINE;
        Group group = loadGroupViaDisk(templates);

        Context duh = group.getTemplate("duh")
            .createContext()
            .add("chars", new String[]{ "a", "b", "c", "d", "e" });

        Context top = group.getTemplate("top")
            .createContext()
            .add("d", duh);

        assertWrappedRenderingResult("  x: ab" + NEWLINE + "     cd" + NEWLINE + "     e!", 7, top);
    }

    @Test
    public void testLineDoesNotWrapDueToLiteral()
    {
        String templates =
            "m(args,body) ::= <<@Test public voidfoo(<args; wrap=\"\\n\",separator=\", \">) throws Ick { <body> }>>" +
                NEWLINE;
        Context context = loadGroupFromString(templates).getTemplate("m")
            .createContext()
            .add("args", new String[]{ "a", "b", "c" })
            .add("body", "i=3;");

        // make it wrap because of ") throws Ick { " literal
        int n = "@Test public voidfoo(a, b, c".length();
        String expecting = "@Test public voidfoo(a, b, c) throws Ick { i=3; }";
        assertWrappedRenderingResult(expecting, n, context);
    }

    @Test
    public void testSingleValueWrap() throws IOException
    {
        String templates = "m(args,body) ::= <<{ <body; anchor, wrap=\"\\n\"> }>>" + NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("m")
            .createContext()
            .add("body", "i=3;");

        // make it wrap because of ") throws Ick { " literal
        String expecting = "{ " + NEWLINE + "  i=3; }";
        assertWrappedRenderingResult(expecting, 2, context);
    }

    private void assertWrappedRenderingResult(String expecting, int width, Context context)
    {
        assertEquals(expecting,
            context.render()
                .withLineWrapping(width)
                .intoString());
    }

    @Test
    public void testLineWrapInNestedExpr() throws IOException
    {
        String templates = "top(arrays) ::= <<Arrays: <arrays>done>>" +
            NEWLINE +
            "array(values) ::= <%int[] a = { <values; anchor, wrap=\"\\n\", separator=\",\"> };<\\n>%>" +
            NEWLINE;
        Group group = loadGroupViaDisk(templates);

        Context a = group.getTemplate("array")
            .createContext()
            .add("values", MANY_INTEGERS);

        Context top = group.getTemplate("top")
            .createContext()
            .add("arrays", a)
            .add("arrays", a); // add twice

        String expecting = "Arrays: int[] a = { 3,9,20,2,1,4,6,32,5," +
            NEWLINE +
            "                    6,77,888,2,1,6,32,5," +
            NEWLINE +
            "                    6,77,4,9,20,2,1,4,63," +
            NEWLINE +
            "                    9,20,2,1,4,6,32,5,6," +
            NEWLINE +
            "                    77,6,32,5,6,77,3,9,20," +
            NEWLINE +
            "                    2,1,4,6,32,5,6,77,888," +
            NEWLINE +
            "                    1,6,32,5 };" +
            NEWLINE +
            "int[] a = { 3,9,20,2,1,4,6,32,5,6,77,888," +
            NEWLINE +
            "            2,1,6,32,5,6,77,4,9,20,2,1,4," +
            NEWLINE +
            "            63,9,20,2,1,4,6,32,5,6,77,6," +
            NEWLINE +
            "            32,5,6,77,3,9,20,2,1,4,6,32," +
            NEWLINE +
            "            5,6,77,888,1,6,32,5 };" +
            NEWLINE +
            "done";
        assertWrappedRenderingResult(expecting, 40, top);
    }
}
