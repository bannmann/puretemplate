package com.github.bannmann.puretemplate.test;

import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

import com.github.bannmann.puretemplate.AutoIndentWriter;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.STGroupFile;
import com.github.bannmann.puretemplate.STWriter;

public class TestLineWrap extends BaseTest {
    @Test public void testLineWrap() throws Exception {
        String templates =
                "array(values) ::= <<int[] a = { <values; wrap=\"\\n\", separator=\",\"> };>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("array");
        a.add("values",
                       new int[] {3,9,20,2,1,4,6,32,5,6,77,888,2,1,6,32,5,6,77,
                        4,9,20,2,1,4,63,9,20,2,1,4,6,32,5,6,77,6,32,5,6,77,
                        3,9,20,2,1,4,6,32,5,6,77,888,1,6,32,5});
        String expecting =
            "int[] a = { 3,9,20,2,1,4,6,32,5,6,77,888," + newline +
            "2,1,6,32,5,6,77,4,9,20,2,1,4,63,9,20,2,1," + newline +
            "4,6,32,5,6,77,6,32,5,6,77,3,9,20,2,1,4,6," + newline +
            "32,5,6,77,888,1,6,32,5 };";

        StringWriter sw = new StringWriter();
        STWriter stw = new AutoIndentWriter(sw,newline); // force \n as newline
        stw.setLineWidth(40);
        a.write(stw);
        String result = sw.toString();
        assertEquals(expecting, result);
    }

    @Test public void testLineWrapAnchored() throws Exception {
        String templates =
            "array(values) ::= <<int[] a = { <values; anchor, wrap, separator=\",\"> };>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("array");
        a.add("values",
            new int[] {3,9,20,2,1,4,6,32,5,6,77,888,2,1,6,32,5,6,77,
            4,9,20,2,1,4,63,9,20,2,1,4,6,32,5,6,77,6,32,5,6,77,
            3,9,20,2,1,4,6,32,5,6,77,888,1,6,32,5});
        String expecting =
            "int[] a = { 3,9,20,2,1,4,6,32,5,6,77,888," + newline +
            "            2,1,6,32,5,6,77,4,9,20,2,1,4," + newline +
            "            63,9,20,2,1,4,6,32,5,6,77,6," + newline +
            "            32,5,6,77,3,9,20,2,1,4,6,32," + newline +
            "            5,6,77,888,1,6,32,5 };";
        assertEquals(expecting, a.render(40));
    }

    @Test public void testSubtemplatesAnchorToo() throws Exception {
        String templates =
                "array(values) ::= <<{ <values; anchor, separator=\", \"> }>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        final ST x = new ST("<\\n>{ <stuff; anchor, separator=\",\\n\"> }<\\n>");
        x.groupThatCreatedThisInstance = group;
        x.add("stuff", "1");
        x.add("stuff", "2");
        x.add("stuff", "3");
        ST a = group.getInstanceOf("array");
        a.add("values", new ArrayList<Object>() {{
            add("a"); add(x); add("b");
        }});
        String expecting =
            "{ a, " + newline +
            "  { 1," + newline +
            "    2," + newline +
            "    3 }" + newline +
            "  , b }";
        assertEquals(expecting, a.render(40));
    }

    @Test public void testFortranLineWrap() throws Exception {
        String templates =
                "func(args) ::= <<       FUNCTION line( <args; wrap=\"\\n      c\", separator=\",\"> )>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("func");
        a.add("args",
                  new String[]{"a", "b", "c", "d", "e", "f"});
        String expecting =
            "       FUNCTION line( a,b,c,d," + newline +
            "      ce,f )";
        assertEquals(expecting, a.render(30));
    }

    @Test public void testLineWrapWithDiffAnchor() throws Exception {
        String templates =
                "array(values) ::= <<int[] a = { <{1,9,2,<values; wrap, separator=\",\">}; anchor> };>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("array");
        a.add("values",
                       new int[] {3,9,20,2,1,4,6,32,5,6,77,888,2,1,6,32,5,6,77,
                        4,9,20,2,1,4,63,9,20,2,1,4,6});
        String expecting =
            "int[] a = { 1,9,2,3,9,20,2,1,4," + newline +
            "            6,32,5,6,77,888,2," + newline +
            "            1,6,32,5,6,77,4,9," + newline +
            "            20,2,1,4,63,9,20,2," + newline +
            "            1,4,6 };";
        assertEquals(expecting, a.render(30));
    }

    @Test public void testLineWrapEdgeCase() throws Exception {
        String templates =
            "duh(chars) ::= \"<chars; wrap={<\\n>}>\""+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("duh");
        a.add("chars", new String[]{"a", "b", "c", "d", "e"});
        // lineWidth==3 implies that we can have 3 characters at most
        String expecting =
            "abc" + newline +
            "de";
        assertEquals(expecting, a.render(3));
    }

    @Test public void testLineWrapLastCharIsNewline() throws Exception {
        String templates =
                "duh(chars) ::= <<" + newline +
                "<chars; wrap=\"\\n\"\\>" + newline +
                ">>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("duh");
        a.add("chars", new String[]{"a", "b", newline, "d", "e"});
        // don't do \n if it's last element anyway
        String expecting =
            "ab" + newline+
            "de";
        assertEquals(expecting,a.render(3));
    }

    @Test public void testLineWrapCharAfterWrapIsNewline() throws Exception {
        String templates =
                "duh(chars) ::= <<" + newline +
                "<chars; wrap=\"\\n\"\\>" + newline +
                ">>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("duh");
        a.add("chars", new String[]{"a", "b", "c", newline, "d", "e"});
        // Once we wrap, we must dump chars as we see them.  A newline right
        // after a wrap is just an "unfortunate" event.  People will expect
        // a newline if it's in the data.
        String expecting =
            "abc" + newline +
            "" + newline +
            "de";
        assertEquals(expecting, a.render(3));
    }

    @Test public void testLineWrapForList() throws Exception {
        String templates =
                "duh(data) ::= <<!<data; wrap>!>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("duh");
        a.add("data", new int[] {1,2,3,4,5,6,7,8,9});
        String expecting =
            "!123" + newline +
            "4567" + newline +
            "89!";
        assertEquals(expecting,a.render(4));
    }

    @Test public void testLineWrapForAnonTemplate() throws Exception {
        String templates =
                "duh(data) ::= <<!<data:{v|[<v>]}; wrap>!>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("duh");
        a.add("data", new int[] {1,2,3,4,5,6,7,8,9});
        String expecting =
            "![1][2][3]" + newline + // width=9 is the 3 char; don't break til after ]
            "[4][5][6]" + newline +
            "[7][8][9]!";
        assertEquals(expecting,a.render(9));
    }

    @Test public void testLineWrapForAnonTemplateAnchored() throws Exception {
        String templates =
                "duh(data) ::= <<!<data:{v|[<v>]}; anchor, wrap>!>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("duh");
        a.add("data", new int[] {1,2,3,4,5,6,7,8,9});
        String expecting =
            "![1][2][3]" + newline +
            " [4][5][6]" + newline +
            " [7][8][9]!";
        assertEquals(expecting, a.render(9));
    }

    @Test public void testLineWrapForAnonTemplateComplicatedWrap() throws Exception {
        String templates =
                "top(s) ::= <<  <s>.>>"+
                "str(data) ::= <<!<data:{v|[<v>]}; wrap=\"!+\\n!\">!>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST t = group.getInstanceOf("top");
        ST s = group.getInstanceOf("str");
        s.add("data", new int[] {1,2,3,4,5,6,7,8,9});
        t.add("s", s);
        String expecting =
            "  ![1][2]!+" + newline +
            "  ![3][4]!+" + newline +
            "  ![5][6]!+" + newline +
            "  ![7][8]!+" + newline +
            "  ![9]!.";
        assertEquals(expecting,t.render(9));
    }

    @Test public void testIndentBeyondLineWidth() throws Exception {
        String templates =
                "duh(chars) ::= <<" +newline+
                "    <chars; wrap=\"\\n\">" + newline +
                ">>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("duh");
        a.add("chars", new String[]{"a", "b", "c", "d", "e"});
        //
        String expecting =
            "    a" + newline +
            "    b" + newline +
            "    c" + newline +
            "    d" + newline +
            "    e";
        assertEquals(expecting, a.render(2));
    }

    @Test public void testIndentedExpr() throws Exception {
        String templates =
                "duh(chars) ::= <<" +newline+
                "    <chars; wrap=\"\\n\">" +newline+
                ">>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("duh");
        a.add("chars", new String[]{"a", "b", "c", "d", "e"});
        //
        String expecting =
            "    ab" + newline +
            "    cd" + newline +
            "    e";
        // width=4 spaces + 2 char.
        assertEquals(expecting, a.render(6));
    }

    @Test public void testNestedIndentedExpr() throws Exception {
        String templates =
                "top(d) ::= <<  <d>!>>"+newline+
                "duh(chars) ::= <<" +newline+
                "  <chars; wrap=\"\\n\">" + newline+
                ">>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST top = group.getInstanceOf("top");
        ST duh = group.getInstanceOf("duh");
        duh.add("chars", new String[]{"a", "b", "c", "d", "e"});
        top.add("d", duh);
        String expecting =
            "    ab" + newline +
            "    cd" + newline +
            "    e!";
        // width=4 spaces + 2 char.
        assertEquals(expecting, top.render(6));
    }

    @Test public void testNestedWithIndentAndTrackStartOfExpr() throws Exception {
        String templates =
                "top(d) ::= <<  <d>!>>"+newline+
                "duh(chars) ::= <<" +newline+
                "x: <chars; anchor, wrap=\"\\n\">" +newline+
                ">>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST top = group.getInstanceOf("top");
        ST duh = group.getInstanceOf("duh");
        duh.add("chars", new String[]{"a", "b", "c", "d", "e"});
        top.add("d", duh);
        //
        String expecting =
            "  x: ab" + newline +
            "     cd" + newline +
            "     e!";
        assertEquals(expecting, top.render(7));
    }

    @Test public void testLineDoesNotWrapDueToLiteral() throws Exception {
        String templates =
                "m(args,body) ::= <<@Test public voidfoo(<args; wrap=\"\\n\",separator=\", \">) throws Ick { <body> }>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST a = group.getInstanceOf("m");
        a.add("args",
                  new String[]{"a", "b", "c"});
        a.add("body", "i=3;");
        // make it wrap because of ") throws Ick { " literal
        int n = "@Test public voidfoo(a, b, c".length();
        String expecting =
            "@Test public voidfoo(a, b, c) throws Ick { i=3; }";
        assertEquals(expecting, a.render(n));
    }

    @Test public void testSingleValueWrap() throws Exception {
        String templates =
                "m(args,body) ::= <<{ <body; anchor, wrap=\"\\n\"> }>>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST m = group.getInstanceOf("m");
        m.add("body", "i=3;");
        // make it wrap because of ") throws Ick { " literal
        String expecting =
            "{ " + newline+
            "  i=3; }";
        assertEquals(expecting, m.render(2));
    }

    @Test public void testLineWrapInNestedExpr() throws Exception {
        String templates =
                "top(arrays) ::= <<Arrays: <arrays>done>>"+newline+
                "array(values) ::= <%int[] a = { <values; anchor, wrap=\"\\n\", separator=\",\"> };<\\n>%>"+newline;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");

        ST top = group.getInstanceOf("top");
        ST a = group.getInstanceOf("array");
        a.add("values",
                       new int[] {3,9,20,2,1,4,6,32,5,6,77,888,2,1,6,32,5,6,77,
                        4,9,20,2,1,4,63,9,20,2,1,4,6,32,5,6,77,6,32,5,6,77,
                        3,9,20,2,1,4,6,32,5,6,77,888,1,6,32,5});
        top.add("arrays", a);
        top.add("arrays", a); // add twice
        String expecting =
            "Arrays: int[] a = { 3,9,20,2,1,4,6,32,5," + newline +
            "                    6,77,888,2,1,6,32,5," + newline +
            "                    6,77,4,9,20,2,1,4,63," + newline +
            "                    9,20,2,1,4,6,32,5,6," + newline +
            "                    77,6,32,5,6,77,3,9,20," + newline +
            "                    2,1,4,6,32,5,6,77,888," + newline +
            "                    1,6,32,5 };" + newline +
            "int[] a = { 3,9,20,2,1,4,6,32,5,6,77,888," + newline +
            "            2,1,6,32,5,6,77,4,9,20,2,1,4," + newline +
            "            63,9,20,2,1,4,6,32,5,6,77,6," + newline +
            "            32,5,6,77,3,9,20,2,1,4,6,32," + newline +
            "            5,6,77,888,1,6,32,5 };" + newline +
            "done";
        assertEquals(expecting, top.render(40));
    }
}
