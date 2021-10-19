package com.example;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.misc.ErrorBuffer;

class TestSubtemplates extends BaseTest
{
    @Test
    void testSimpleIteration()
    {
        String templates = "test(names) ::= << <names:{n | <n>}>! >>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext()
            .add("names", "Ter")
            .add("names", "Tom")
            .add("names", "Sumana");

        assertRenderingResult(" TerTomSumana! ", context);
    }

    @Test
    void testMapIterationIsByKeys()
    {
        String templates = "test(emails) ::= << <emails:{n|<n>}>! >>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext()
            .add("emails", newLinkedHashMap(entry("parrt", "Ter"), entry("tombu", "Tom"), entry("dmose", "Dan")));

        assertRenderingResult(" parrttombudmose! ", context);
    }

    @SafeVarargs
    private <K, V> Map<K, V> newLinkedHashMap(Map.Entry<K, V>... entries)
    {
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Test
    void testNestedIterationWithArg()
    {
        String templates = "test(users) ::= << <users:{u | <u.id:{id | <id>=}><u.name>}>! >>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext()
            .add("users", new User(1, "parrt"))
            .add("users", new User(2, "tombu"))
            .add("users", new User(3, "sri"));

        assertRenderingResult(" 1=parrt2=tombu3=sri! ", context);
    }

    @Test
    void testSubtemplateAsDefaultArg()
    {
        String templates = "t(x,y={<x:{s|<s><s>}>}) ::= <<\n" + "x: <x>\n" + "y: <y>\n" + ">>" + NEWLINE;
        Context context = loadGroupFromString(templates).getTemplate("t")
            .createContext()
            .add("x", "a");
        assertRenderingResult("x: a" + NEWLINE + "y: aa", context);
    }

    @Test
    void testParallelAttributeIteration()
    {
        Context context = makeTemplateContext("<names,phones,salaries:{n,p,s | <n>@<p>: <s>\n}>").add("names", "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2")
            .add("salaries", "big")
            .add("salaries", "huge");
        assertRenderingResult("Ter@1: big" + NEWLINE + "Tom@2: huge" + NEWLINE, context);
    }

    @Test
    void testParallelAttributeIterationWithNullValue()
    {
        Context context = makeTemplateContext("<names,phones,salaries:{n,p,s | <n>@<p>: <s>\n}>").add("names", "Ter")
            .add("names", "Tom")
            .add("names", "Sriram")
            .add("phones", Arrays.asList("1", null, "3"))
            .add("salaries", "big")
            .add("salaries", "huge")
            .add("salaries", "enormous");
        assertRenderingResult("Ter@1: big" + NEWLINE + "Tom@: huge" + NEWLINE + "Sriram@3: enormous" + NEWLINE,
            context);
    }

    @Test
    void testParallelAttributeIterationHasI()
    {
        Context context = makeTemplateContext("<names,phones,salaries:{n,p,s | <i0>. <n>@<p>: <s>\n}>").add("names",
                "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2")
            .add("salaries", "big")
            .add("salaries", "huge");
        assertRenderingResult("0. Ter@1: big" + NEWLINE + "1. Tom@2: huge" + NEWLINE, context);
    }

    @Test
    void testParallelAttributeIterationWithDifferentSizes()
    {
        Context context = makeTemplateContext("<names,phones,salaries:{n,p,s | <n>@<p>: <s>}; separator=\", \">").add(
                "names",
                "Ter")
            .add("names", "Tom")
            .add("names", "Sriram")
            .add("phones", "1")
            .add("phones", "2")
            .add("salaries", "big");
        assertRenderingResult("Ter@1: big, Tom@2: , Sriram@: ", context);
    }

    @Test
    void testParallelAttributeIterationWithSingletons()
    {
        Context context = makeTemplateContext("<names,phones,salaries:{n,p,s | <n>@<p>: <s>}; separator=\", \">").add(
                "names",
                "Ter")
            .add("phones", "1")
            .add("salaries", "big");
        assertRenderingResult("Ter@1: big", context);
    }

    @Test
    void testParallelAttributeIterationWithDifferentSizesTemplateRefInsideToo()
    {
        String templates = "page(names,phones,salaries) ::= " +
            NEWLINE +
            "   << <names,phones,salaries:{n,p,s | <value(n)>@<value(p)>: <value(s)>}; separator=\", \"> >>" +
            NEWLINE +
            "value(x) ::= \"<if(!x)>n/a<else><x><endif>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("page")
            .createContext()
            .add("names", "Ter")
            .add("names", "Tom")
            .add("names", "Sriram")
            .add("phones", "1")
            .add("phones", "2")
            .add("salaries", "big");

        assertRenderingResult(" Ter@1: big, Tom@2: n/a, Sriram@n/a: n/a ", context);
    }

    @Test
    void testEvalSTIteratingSubtemplateInSTFromAnotherGroup() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();

        String inner = "test(m) ::= <<[<m:samegroup()>]>>" + NEWLINE + "samegroup(x) ::= <<hi >>" + NEWLINE;
        Context test = loadGroupViaDisk(inner, errors).getTemplate("test")
            .createContext()
            .add("m", new int[]{ 1, 2, 3 });

        String outer = "errorMessage(x) ::= <<#<x>#>>" + NEWLINE;
        Context errorMessage = loadGroupViaDisk(outer, errors).getTemplate("errorMessage")
            .createContext()
            .add("x", test);

        assertRenderingResult("#[hi hi hi ]#", errorMessage);
        assertEquals(0,
            errors.getErrors()
                .size()); // ignores no such prop errors
    }

    @Test
    void testEvalSTIteratingSubtemplateInSTFromAnotherGroupSingleValue() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();

        String inner = "test(m) ::= <<[<m:samegroup()>]>>" + NEWLINE + "samegroup(x) ::= <<hi >>" + NEWLINE;
        Context test = loadGroupViaDisk(inner, errors).getTemplate("test")
            .createContext()
            .add("m", 10);

        String outer = "errorMessage(x) ::= <<#<x>#>>" + NEWLINE;
        Context errorMessage = loadGroupViaDisk(outer, errors).getTemplate("errorMessage")
            .createContext()
            .add("x", test);

        assertRenderingResult("#[hi ]#", errorMessage);
        assertEquals(0,
            errors.getErrors()
                .size()); // ignores no such prop errors
    }

    @Test
    void testEvalSTFromAnotherGroup() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();

        String inner = "bob() ::= <<inner>>" + NEWLINE;
        Context bob = loadGroupViaDisk(inner, errors).getTemplate("bob")
            .createContext();

        String outer = "errorMessage(x) ::= << <x> >>" + NEWLINE + "bob() ::= <<outer>>" + NEWLINE;
        Context errorMessage = loadGroupViaDisk(outer, errors).getTemplate("errorMessage")
            .createContext()
            .add("x", bob);

        assertRenderingResult(" inner ", errorMessage);
        assertEquals(0,
            errors.getErrors()
                .size()); // ignores no such prop errors
    }
}
