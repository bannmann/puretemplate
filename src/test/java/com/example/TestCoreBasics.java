package com.example;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Template;

@Slf4j
class TestCoreBasics extends BaseTest
{
    @Test
    void testPassThru()
    {
        Context context = loader.getGroup()
            .fromString("a(x,y) ::= \"<b(...)>\"\n" + "b(x,y) ::= \"<x><y>\"\n")
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "x")
            .add("y", "y");
        assertRenderingResult("xy", context);
    }

    @Test
    void testPassThruWithDefaultValue()
    {
        Context context = loader.getGroup()
            .fromString("a(x,y) ::= \"<b(...)>\"\nb(x,y={99}) ::= \"<x><y>\"\n") // 'a' should not set y when it sees "no value" from above
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "x");
        assertRenderingResult("x99", context);
    }

    @Test
    void testPassThruWithDefaultValueThatLacksDefinitionAbove()
    {
        Context context = loader.getGroup()
            .fromString("a(x) ::= \"<b(...)>\"\nb(x,y={99}) ::= \"<x><y>\"\n") // 'a' should not set y when it sees "no definition" from above
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "x");
        assertRenderingResult("x99", context);
    }

    @Test
    void testPassThruPartialArgs()
    {
        Context context = loader.getGroup()
            .fromString("a(x,y) ::= \"<b(y={99},...)>\"\n" + "b(x,y) ::= \"<x><y>\"\n")
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "x")
            .add("y", "y");
        assertRenderingResult("x99", context);
    }

    @Test
    void testPassThruNoMissingArgs()
    {
        Context context = loader.getGroup()
            .fromString("a(x,y) ::= \"<b(y={99},x={1},...)>\"\n" + "b(x,y) ::= \"<x><y>\"\n")
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "x")
            .add("y", "y");
        assertRenderingResult("199", context);
    }

    @Test
    void testMapWithExprAsTemplateName()
    {
        String templates = "d ::= [\"foo\":\"bold\"]\n" +
            "test(name) ::= \"<name:(d.foo)()>\"\n" +
            "bold(x) ::= <<*<x>*>>\n";

        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext()
            .add("name", "Ter")
            .add("name", "Tom")
            .add("name", "Sumana");

        assertRenderingResult("*Ter**Tom**Sumana*", context);
    }

    @Test
    void testTrueCond()
    {
        Context context = makeTemplateContext("<if(name)>works<endif>").add("name", "Ter");
        assertRenderingResult("works", context);
    }

    @Test
    void testEmptyIFTemplate()
    {
        Context context = makeTemplateContext("<if(x)>fail<elseif(name)><endif>").add("name", "Ter");
        assertRenderingResult("", context);
    }

    @Test
    void testCondParens()
    {
        assertNoArgRenderingResult("works", "<if(!(x||y)&&!z)>works<endif>");
    }

    @Test
    void testFalseCond()
    {
        assertNoArgRenderingResult("", "<if(name)>works<endif>");
    }

    @Test
    void testFalseCond2()
    {
        Context context = makeTemplateContext("<if(name)>works<endif>").add("name", null);
        assertRenderingResult("", context);
    }

    @Test
    void testFalseCondWithFormalArgs()
    {
        String groupFile = "a(scope) ::= <<" +
            NEWLINE +
            "foo" +
            NEWLINE +
            "    <if(scope)>oops<endif>" +
            NEWLINE +
            "bar" +
            NEWLINE +
            ">>";
        Template template = loadGroupFromString(groupFile).getTemplate("a");
        dump(log, template);
        assertRenderingResult("foo" + NEWLINE + "bar", template.createContext());
    }

    @Test
    void testElseIf2()
    {
        Context context = makeTemplateContext("<if(x)>fail1<elseif(y)>fail2<elseif(z)>works<else>fail3<endif>").add("z",
            "blort");
        assertRenderingResult("works", context);
    }

    @Test
    void testElseIf3()
    {
        Context context = makeTemplateContext("<if(x)><elseif(y)><elseif(z)>works<else><endif>").add("z", "blort");
        assertRenderingResult("works", context);
    }

    @Test
    void testNotTrueCond()
    {
        Context context = makeTemplateContext("<if(!name)>works<endif>").add("name", "Ter");
        assertRenderingResult("", context);
    }

    @Test
    void testNotFalseCond()
    {
        assertNoArgRenderingResult("works", "<if(!name)>works<endif>");
    }

    @Test
    void testParensInConditonal()
    {
        Context context = makeTemplateContext("<if((a||b)&&(c||d))>works<endif>").add("a", true)
            .add("b", true)
            .add("c", true)
            .add("d", true);

        assertRenderingResult("works", context);
    }

    @Test
    void testParensInConditonal2()
    {
        Context context = makeTemplateContext("<if((!a||b)&&!(c||d))>broken<else>works<endif>").add("a", true)
            .add("b", true)
            .add("c", true)
            .add("d", true);

        assertRenderingResult("works", context);
    }

    @Test
    void testTrueCondWithElse()
    {
        Context context = makeTemplateContext("<if(name)>works<else>fail<endif>").add("name", "Ter");
        assertRenderingResult("works", context);
    }

    @Test
    void testFalseCondWithElse()
    {
        assertNoArgRenderingResult("works", "<if(name)>fail<else>works<endif>");
    }

    @Test
    void testElseIf()
    {
        Context context = makeTemplateContext("<if(name)>fail<elseif(id)>works<else>fail<endif>").add("id", "2DF3DF");
        assertRenderingResult("works", context);
    }

    @Test
    void testElseIfNoElseAllFalse()
    {
        assertNoArgRenderingResult("", "<if(name)>fail<elseif(id)>fail<endif>");
    }

    @Test
    void testElseIfAllExprFalse()
    {
        assertNoArgRenderingResult("works", "<if(name)>fail<elseif(id)>fail<else>works<endif>");
    }

    @Test
    void testOr()
    {
        Context context = makeTemplateContext("<if(name||notThere)>works<else>fail<endif>").add("name", "Ter");
        assertRenderingResult("works", context);
    }

    @Test
    void testMapConditionAndEscapeInside()
    {
        Context context = makeTemplateContext("<if(m.name)>works \\\\<endif>").add("m", Map.of("name", "Ter"));
        assertRenderingResult("works \\", context);
    }

    @Test
    void testAnd()
    {
        Context context = makeTemplateContext("<if(name&&notThere)>fail<else>works<endif>").add("name", "Ter");
        assertRenderingResult("works", context);
    }

    @Test
    void testAndNot()
    {
        Context context = makeTemplateContext("<if(name&&!notThere)>works<else>fail<endif>").add("name", "Ter");
        assertRenderingResult("works", context);
    }

    @Test
    void testUnicodeLiterals()
    {
        assertNoArgRenderingResult("Foo \ufea5" + NEWLINE + "\u00C2 bar" + NEWLINE,
            "Foo <\\uFEA5><\\n><\\u00C2> bar\n");

        assertNoArgRenderingResult("Foo \ufea5" + NEWLINE + "\u00C2 bar" + NEWLINE,
            "Foo <\\uFEA5><\\n><\\u00C2> bar" + NEWLINE);

        assertNoArgRenderingResult("Foo bar" + NEWLINE, "Foo<\\ >bar<\\n>");
    }

    @Test
    void testSubtemplateExpr()
    {
        assertNoArgRenderingResult("name" + NEWLINE, "<{name\n}>");
    }

    @Test
    void testArrayOfTemplates()
    {
        Context context = makeTemplateContext("<foo>!");
        Context[] foo = new Context[]{ makeTemplateContext("hi"), makeTemplateContext("mom") };
        context.add("foo", foo);

        assertRenderingResult("himom!", context);
    }

    @Test
    void testArrayOfTemplatesInTemplate()
    {
        Context context = makeTemplateContext("<foo>!");

        Context[] foo = new Context[]{ makeTemplateContext("hi"), makeTemplateContext("mom") };
        context.add("foo", foo);

        Context wrapper = makeTemplateContext("<x>").add("x", context);
        assertRenderingResult("himom!", wrapper);
    }

    @Test
    void testListOfTemplates()
    {
        Context context = makeTemplateContext("<foo>!");

        List<Context> foo = List.of(makeTemplateContext("hi"), makeTemplateContext("mom"));
        context.add("foo", foo);

        assertRenderingResult("himom!", context);
    }

    @Test
    void testListOfTemplatesInTemplate()
    {
        Context context = makeTemplateContext("<foo>!");

        List<Context> foo = List.of(makeTemplateContext("hi"), makeTemplateContext("mom"));
        context.add("foo", foo);

        Context wrapper = makeTemplateContext("<x>").add("x", context);
        assertRenderingResult("himom!", wrapper);
    }
}
