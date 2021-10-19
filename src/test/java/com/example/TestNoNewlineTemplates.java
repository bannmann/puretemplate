package com.example;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;

class TestNoNewlineTemplates extends BaseTest
{
    @Test
    void testNoNewlineTemplate()
    {
        Context context = loader.getGroup()
            .fromString("t(x) ::= <%\n" +
                "[  <if(!x)>" +
                "<else>" +
                "<x>\n" +
                "<endif>" +
                "\n" +
                "\n" +
                "]\n" +
                "\n" +
                "%>\n")
            .build()
            .getTemplate("t")
            .createContext()
            .add("x", 99);
        assertRenderingResult("[  99]", context);
    }

    @Test
    void testWSNoNewlineTemplate()
    {
        Context context = loader.getGroup()
            .fromString("t(x) ::= <%\n" + "\n" + "%>\n")
            .build()
            .getTemplate("t")
            .createContext()
            .add("x", 99);
        assertRenderingResult("", context);
    }

    @Test
    void testEmptyNoNewlineTemplate()
    {
        Context context = loader.getGroup()
            .fromString("t(x) ::= <%%>\n")
            .build()
            .getTemplate("t")
            .createContext()
            .add("x", 99);
        assertRenderingResult("", context);
    }

    @Test
    void testIgnoreIndent()
    {
        Context context = loader.getGroup()
            .fromString("t(x) ::= <%\n" + "   foo\n" + "   <x>\n" + "%>\n")
            .build()
            .getTemplate("t")
            .createContext()
            .add("x", 99);
        assertRenderingResult("foo99", context);
    }

    @Test
    void testIgnoreIndentInIF()
    {
        Context context = loader.getGroup()
            .fromString("t(x) ::= <%\n" + "   <if(x)>\n" + "       foo\n" + "   <endif>\n" + "   <x>\n" + "%>\n")
            .build()
            .getTemplate("t")
            .createContext()
            .add("x", 99);
        assertRenderingResult("foo99", context);
    }

    @Test
    void testKeepWS()
    {
        Context context = loader.getGroup()
            .fromString("t(x) ::= <%\n" + "   <x> <x> hi\n" + "%>\n")
            .build()
            .getTemplate("t")
            .createContext()
            .add("x", 99);
        assertRenderingResult("99 99 hi", context);
    }

    @Test
    void testRegion()
    {
        Context context = loader.getGroup()
            .fromString("t(x) ::= <%\n" +
                "<@r>\n" +
                "   Ignore\n" +
                "   newlines and indents\n" +
                "<x>\n\n\n" +
                "<@end>\n" +
                "%>\n")
            .build()
            .getTemplate("t")
            .createContext()
            .add("x", 99);
        assertRenderingResult("Ignorenewlines and indents99", context);
    }
}
