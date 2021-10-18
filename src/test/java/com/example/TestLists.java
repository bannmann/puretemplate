package com.example;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;

public class TestLists extends BaseTest
{
    @Test
    public void testJustCat()
    {
        Context context = makeTemplateContext("<[names,phones]>").add("names", "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("TerTom12", context);
    }

    @Test
    public void testListLiteralWithEmptyElements()
    {
        Context context = makeTemplateContext("<[\"Ter\",,\"Jesse\"]:{n | <i>:<n>}; separator=\", \", null={foo}>");
        assertRenderingResult("1:Ter, foo, 2:Jesse", context);
    }

    @Test
    public void testListLiteralWithEmptyFirstElement()
    {
        Context context = makeTemplateContext("<[,\"Ter\",\"Jesse\"]:{n | <i>:<n>}; separator=\", \", null={foo}>");
        assertRenderingResult("foo, 1:Ter, 2:Jesse", context);
    }

    @Test
    public void testLength()
    {
        Context context = makeTemplateContext("<length([names,phones])>").add("names", "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("4", context);
    }

    @Test
    public void testCat2Attributes()
    {
        Context context = makeTemplateContext("<[names,phones]; separator=\", \">").add("names", "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("Ter, Tom, 1, 2", context);
    }

    @Test
    public void testCat2AttributesWithApply()
    {
        Context context = makeTemplateContext("<[names,phones]:{a|<a>.}>").add("names", "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("Ter.Tom.1.2.", context);
    }

    @Test
    public void testCat3Attributes()
    {
        Context context = makeTemplateContext("<[names,phones,salaries]; separator=\", \">").add("names", "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2")
            .add("salaries", "big")
            .add("salaries", "huge");
        assertRenderingResult("Ter, Tom, 1, 2, big, huge", context);
    }

    @Test
    public void testCatWithTemplateApplicationAsElement()
    {
        Context context = makeTemplateContext("<[names:{n|<n>!},phones]; separator=\", \">").add("names", "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("Ter!, Tom!, 1, 2", context);
    }

    @Test
    public void testCatWithIFAsElement()
    {
        Context context = makeTemplateContext("<[{<if(names)>doh<endif>},phones]; separator=\", \">").add("names",
            "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("doh, 1, 2", context);
    }

    @Test
    public void testCatNullValues()
    {
        // [a, b] must behave like <a><b>; if a==b==null, blank output
        // unless null argument.
        Context context = makeTemplateContext("<[no,go]; null=\"foo\", separator=\", \">").add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("foo, foo", context);
    }

    @Test
    public void testCatWithNullTemplateApplicationAsElement()
    {
        Context context = makeTemplateContext("<[names:{n|<n>!},\"foo\"]:{a|x}; separator=\", \">").add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("x", context);
    }

    @Test
    public void testCatWithNestedTemplateApplicationAsElement()
    {
        Context context = makeTemplateContext("<[names, [\"foo\",\"bar\"]:{x | <x>!},phones]; separator=\", \">").add(
            "names",
            "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("Ter, Tom, foo!, bar!, 1, 2", context);
    }

    @Test
    public void testListAsTemplateArgument()
    {
        String templates = "test(names,phones) ::= \"<foo([names,phones])>\"" +
            NEWLINE +
            "foo(items) ::= \"<items:{a | *<a>*}>\"" +
            NEWLINE;
        Context context = loadGroupFromString(templates).getTemplate("test")
            .createContext()
            .add("names", "Ter")
            .add("names", "Tom")
            .add("phones", "1")
            .add("phones", "2");
        assertRenderingResult("*Ter**Tom**1**2*", context);
    }

    @Test
    public void testListWithTwoEmptyListsCollapsesToEmptyList()
    {
        Context context = makeTemplateContext("<[[],[]]:{x | <x>!}; separator=\", \">").add("names", "Ter")
            .add("names", "Tom");
        assertRenderingResult("", context);
    }
}
