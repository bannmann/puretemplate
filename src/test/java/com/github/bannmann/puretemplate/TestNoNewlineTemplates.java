package com.github.bannmann.puretemplate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNoNewlineTemplates extends BaseTest {
        @Test
        public void testNoNewlineTemplate() throws Exception {
        String template =
            "t(x) ::= <%\n" +
            "[  <if(!x)>" +
            "<else>" +
            "<x>\n" +
            "<endif>" +
            "\n" +
            "\n" +
            "]\n" +
            "\n" +
            "%>\n";
        STGroup g = new STGroupString(template);
        ST st = g.getInstanceOf("t");
        st.add("x", 99);
        String expected = "[  99]";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testWSNoNewlineTemplate() throws Exception {
        String template =
            "t(x) ::= <%\n" +
            "\n" +
            "%>\n";
        STGroup g = new STGroupString(template);
        ST st = g.getInstanceOf("t");
        st.add("x", 99);
        String expected = "";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testEmptyNoNewlineTemplate() throws Exception {
        String template =
            "t(x) ::= <%%>\n";
        STGroup g = new STGroupString(template);
        ST st = g.getInstanceOf("t");
        st.add("x", 99);
        String expected = "";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testIgnoreIndent() throws Exception {
        String template =
            "t(x) ::= <%\n" +
            "   foo\n" +
            "   <x>\n" +
            "%>\n";
        STGroup g = new STGroupString(template);
        ST st = g.getInstanceOf("t");
        st.add("x", 99);
        String expected = "foo99";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testIgnoreIndentInIF() throws Exception {
        String template =
            "t(x) ::= <%\n" +
            "   <if(x)>\n" +
            "       foo\n" +
            "   <endif>\n" +
            "   <x>\n" +
            "%>\n";
        STGroup g = new STGroupString(template);
        ST st = g.getInstanceOf("t");
        st.add("x", 99);
        String expected = "foo99";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testKeepWS() throws Exception {
        String template =
            "t(x) ::= <%\n" +
            "   <x> <x> hi\n" +
            "%>\n";
        STGroup g = new STGroupString(template);
        ST st = g.getInstanceOf("t");
        st.add("x", 99);
        String expected = "99 99 hi";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testRegion() throws Exception {
        String template =
            "t(x) ::= <%\n" +
            "<@r>\n" +
            "   Ignore\n" +
            "   newlines and indents\n" +
            "<x>\n\n\n" +
            "<@end>\n" +
            "%>\n";
        STGroup g = new STGroupString(template);
        ST st = g.getInstanceOf("t");
        st.add("x", 99);
        String expected = "Ignorenewlines and indents99";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testDefineRegionInSubgroup() throws Exception {
        String dir = getRandomDir();
        String g1 = "a() ::= <<[<@r()>]>>\n";
        writeFile(dir, "g1.stg", g1);
        String g2 = "@a.r() ::= <%\n" +
        "   foo\n\n\n" +
        "%>\n";
        writeFile(dir, "g2.stg", g2);

        STGroup group1 = new STGroupFile(dir+"/g1.stg");
        STGroup group2 = new STGroupFile(dir+"/g2.stg");
        group2.importTemplates(group1); // define r in g2
        ST st = group2.getInstanceOf("a");
        String expected = "[foo]";
        String result = st.render();
        assertEquals(expected, result);
    }

}
