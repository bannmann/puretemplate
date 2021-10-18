package com.example;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.misc.ErrorBuffer;

public class TestScopes extends BaseTest
{
    @Test
    public void testSeesEnclosingAttr() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();

        Context context = loadGroupViaDisk("t(x,y) ::= \"<u()>\"\n" + "u() ::= \"<x><y>\"", errors).getTemplate("t")
            .createContext()
            .add("x", "x")
            .add("y", "y");

        assertEquals("", errors.toString());
        assertRenderingResult("xy", context);
    }

    @Test
    public void testMissingArg() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();

        Context context = loadGroupViaDisk("t() ::= \"<u()>\"\n" + "u(z) ::= \"\"", errors).getTemplate("t")
            .createContext();

        context.render()
            .intoString();

        String expectedError = "context [/t] 1:1 passed 0 arg(s) to template /u with 1 declared arg(s)" + NEWLINE;
        assertEquals(expectedError, errors.toString());
    }

    @Test
    public void testUnknownAttr() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();

        Context context = loadGroupViaDisk("t() ::= \"<x>\"\n", errors).getTemplate("t")
            .createContext();

        context.render()
            .intoString();

        String expectedError = "context [/t] 1:1 attribute x isn't defined" + NEWLINE;
        assertEquals(expectedError, errors.toString());
    }

    @Test
    public void testArgWithSameNameAsEnclosing() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();

        Context context = loadGroupViaDisk("t(x,y) ::= \"<u(x)>\"\n" + "u(y) ::= \"<x><y>\"", errors).getTemplate("t")
            .createContext()
            .add("x", "x")
            .add("y", "y");

        assertRenderingResult("xx", context);

        assertEquals("", errors.toString());
    }

    @Test
    public void testIndexAttrVisibleLocallyOnly() throws IOException
    {
        String templates = "t(names) ::= \"<names:{n | <u(n)>}>\"\n" + "u(x) ::= \"<i>:<x>\"";
        ErrorBuffer errors = new ErrorBuffer();

        Context context = loadGroupViaDisk(templates, errors).getTemplate("t")
            .createContext()
            .add("names", "Ter");

        assertEquals("group.stg 2:11: implicitly-defined attribute i not visible" + NEWLINE, errors.toString());
        assertRenderingResult(":Ter", context);
    }
}
