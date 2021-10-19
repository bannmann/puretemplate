package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.misc.ErrorBuffer;

class TestIndirectionAndEarlyEval extends BaseTest
{
    @Test
    void testEarlyEval()
    {
        Context context = makeTemplateContext("<(name)>").add("name", "Ter");
        assertRenderingResult("Ter", context);
    }

    @Test
    void testIndirectTemplateInclude()
    {
        String templates = "foo() ::= <<bar>>" + NEWLINE + "test(name) ::= << <(name)()> >>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext()
            .add("name", "foo");

        assertRenderingResult(" bar ", context);
    }

    @Test
    void testIndirectTemplateIncludeWithArgs()
    {
        String templates = "foo(x, y) ::= << <x><y> >>" + NEWLINE + "test(name) ::= <<#<(name)({1},{2})>#>>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("#12 #", context);
    }

    @Test
    void testIndirectCallWithPassThru() throws IOException
    {
        // pass-through for dynamic template invocation is not supported by the bytecode representation
        ErrorBuffer errors = new ErrorBuffer();

        Group group = loadGroupViaDisk("t1(x) ::= \"<x>\"\n" +
            "main(x=\"hello\",t=\"t1\") ::= <<\n" +
            "<(t)(...)>\n" +
            ">>", errors);

        assertThrowsIllegalArgumentException(() -> group.getTemplate("main"));
        assertEquals("group.stg 2:34: mismatched input '...' expecting RPAREN" + NEWLINE, errors.toString());
    }

    @Test
    void testIndirectTemplateIncludeViaTemplate()
    {
        String templates = "foo() ::= <<bar>>" +
            NEWLINE +
            "tname() ::= <<foo>>" +
            NEWLINE +
            "test(name) ::= << <(tname())()> >>" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext();

        assertRenderingResult(" bar ", context);
    }

    @Test
    void testIndirectProp()
    {
        Context context = makeTemplateContext("<u.(propname)>: <u.name>").add("u", new User(1, "parrt"))
            .add("propname", "id");
        assertRenderingResult("1: parrt", context);
    }

    @Test
    void testIndirectMap()
    {
        String templates = "a(x) ::= <<[<x>]>>" +
            NEWLINE +
            "test(names,templateName) ::= <<hi <names:(templateName)()>!>>" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext()
            .add("names", "Ter")
            .add("names", "Tom")
            .add("names", "Sumana")
            .add("templateName", "a");

        assertRenderingResult("hi [Ter][Tom][Sumana]!", context);
    }

    @Test
    void testNonStringDictLookup()
    {
        Context context = makeTemplateContext("<m.(intkey)>").add("m", Collections.singletonMap(36, "foo"))
            .add("intkey", 36);
        assertRenderingResult("foo", context);
    }
}
