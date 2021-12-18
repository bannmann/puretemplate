package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.misc.ErrorBuffer;

@Slf4j
class TestScopes extends BaseTest
{
    @Test
    void testSeesEnclosingAttr() throws IOException
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
    void testMissingArg() throws IOException
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
    void testUnknownAttr() throws IOException
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
    void testArgWithSameNameAsEnclosing() throws IOException
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
    void testIndexAttrVisibleLocallyOnly()
    {
        String templates = "t(names) ::= \"<names:{n | <u(n)>}>\"\n" + "u(x) ::= \"<i>:<x>\"";
        ErrorBuffer errors = new ErrorBuffer();
        Group group = loadGroupFromString(templates, errors);

        dump(log, group.getTemplate("u"));

        Context context = group.getTemplate("t")
            .createContext()
            .add("names", "Ter");

        assertRenderingResult(":Ter", context);

        String expectedError = "<string> 2:11: implicitly-defined attribute i not visible" + NEWLINE;
        assertEquals(expectedError, errors.toString());
    }
}
