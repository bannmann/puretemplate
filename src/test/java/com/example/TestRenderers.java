package com.example;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.puretemplate.Loader.RendererDepth.NON_RECURSIVE;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.Template;
import org.puretemplate.misc.ErrorBuffer;
import org.puretemplate.model.AttributeRenderer;
import org.puretemplate.model.ModelAdaptor;

class TestRenderers extends BaseTest
{
    private static final ModelAdaptor<? super String> MODEL_ADAPTOR = (model, property, propertyName) -> "yay";

    private static String toUpperCaseRenderer(String value, String formatString, Locale locale)
    {
        return value.toUpperCase();
    }

    private AttributeRenderer<Number> mockNumberRenderer;
    private AttributeRenderer<String> mockStringRenderer;

    @Override
    @BeforeEach
    public void setUp()
    {
        super.setUp();

        mockNumberRenderer = mockAttributeRenderer();
        mockStringRenderer = mockAttributeRenderer();
    }

    @SuppressWarnings("unchecked")
    private <T> AttributeRenderer<T> mockAttributeRenderer()
    {
        AttributeRenderer<T> mock = Mockito.mock(AttributeRenderer.class);
        when(mock.render(any(), any(), any(Locale.class))).thenReturn("");
        return mock;
    }

    @Test
    void testRendererWithSystemDefaultLocale()
    {
        String template = "<arg>";

        Locale originalDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
        try
        {
            getTemplateWithRenderer(template, Number.class, mockNumberRenderer).createContext()
                .useSystemDefaultLocale()
                .add("arg", 1337)
                .render()
                .intoString();

            verify(mockNumberRenderer).render(1337, null, Locale.GERMANY);
        }
        finally
        {
            Locale.setDefault(originalDefaultLocale);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private <T> Template getTemplateWithRenderer(String template, Class<T> type, AttributeRenderer<? super T> renderer)
    {
        Group group = loader.getGroup()
            .blank()
            .registerAttributeRenderer(type, renderer, NON_RECURSIVE)
            .build();

        return getTemplateAttachedToGroup(template, group);
    }

    private Template getTemplateAttachedToGroup(String template, Group group)
    {
        return loader.getTemplate()
            .fromString(template)
            .attachedToGroup(group)
            .build();
    }

    @Test
    void testRootLocale()
    {
        String template = "<arg>";

        getTemplateWithRenderer(template, Number.class, mockNumberRenderer).createContext()
            .add("arg", 1337)
            .render()
            .intoString();

        verify(mockNumberRenderer).render(1337, null, Locale.ROOT);
    }

    @Test
    void testExplicitLocale()
    {
        String template = "<arg>";

        getTemplateWithRenderer(template, Number.class, mockNumberRenderer).createContext()
            .setLocale(Locale.JAPAN)
            .add("arg", 1337)
            .render()
            .intoString();

        verify(mockNumberRenderer).render(1337, null, Locale.JAPAN);
    }

    @Test
    void testRendererWithTemplateInclude()
    {
        // must toString the t() ref before applying format
        String templates = "foo(x) ::= << <(t()); format=\"quux\"> >>\n" + "t() ::= <<ack>>\n";

        getGroupWithRenderer(String.class, mockStringRenderer, templates).getTemplate("foo")
            .createContext()
            .render()
            .intoString();

        verify(mockStringRenderer).render(eq("ack"), eq("quux"), any());
    }

    private <T> Group getGroupWithRenderer(
        @SuppressWarnings("SameParameterValue") Class<T> type, AttributeRenderer<? super T> renderer, String templates)
    {
        return loader.getGroup()
            .fromString(templates)
            .registerAttributeRenderer(type, renderer, NON_RECURSIVE)
            .build();
    }

    @Test
    void testRendererWithSubtemplateInclude()
    {
        String template = "<({ack}); format=\"quux\">";

        getTemplateWithRenderer(template, String.class, mockStringRenderer).createContext()
            .render()
            .intoString();

        verify(mockStringRenderer).render(eq("ack"), eq("quux"), any());
    }

    @Test
    void testInstanceofRenderer()
    {
        String template = "numbers: <x>, <y>; <z>";

        Context context = getTemplateWithRenderer(template, Number.class, mockNumberRenderer).createContext()
            .add("x", -2100)
            .add("y", 3.14159)
            .add("z", "hi");

        when(mockNumberRenderer.render(eq(-2100), any(), any())).thenReturn("A");
        when(mockNumberRenderer.render(eq(3.14159), any(), any())).thenReturn("B");

        assertRenderingResult("numbers: A, B; hi", context);
    }

    @Test
    void testRendererWithFormatAndList()
    {
        String template = "The names: <names; format=\"quux\">";

        Context context = getTemplateWithRenderer(template, String.class, mockStringRenderer).createContext()
            .add("names", "ter")
            .add("names", "tom")
            .add("names", "sriram");

        when(mockStringRenderer.render(eq("ter"), eq("quux"), any())).thenReturn("A");
        when(mockStringRenderer.render(eq("tom"), eq("quux"), any())).thenReturn("B");
        when(mockStringRenderer.render(eq("sriram"), eq("quux"), any())).thenReturn("C");

        assertRenderingResult("The names: ABC", context);
    }

    @Test
    void testRendererWithFormatAndSeparator()
    {
        String template = "The names: <names; separator=\" and \", format=\"quux\">";

        Context context = getTemplateWithRenderer(template, String.class, mockStringRenderer).createContext()
            .add("names", "ter")
            .add("names", "tom")
            .add("names", "sriram");

        when(mockStringRenderer.render(eq("ter"), eq("quux"), any())).thenReturn("A");
        when(mockStringRenderer.render(eq("tom"), eq("quux"), any())).thenReturn("B");
        when(mockStringRenderer.render(eq("sriram"), eq("quux"), any())).thenReturn("C");

        assertRenderingResult("The names: A and B and C", context);
    }

    @Test
    void testRendererWithFormatAndSeparatorAndNull()
    {
        String template = "The names: <names; separator=\" and \", null=\"n/a\", format=\"quux\">";

        Context context = getTemplateWithRenderer(template, String.class, mockStringRenderer).createContext()
            .add("names", "ter")
            .add("names", null)
            .add("names", "sriram");

        when(mockStringRenderer.render(eq("ter"), eq("quux"), any())).thenReturn("A");
        when(mockStringRenderer.render(eq("n/a"), eq("quux"), any())).thenReturn("X");
        when(mockStringRenderer.render(eq("sriram"), eq("quux"), any())).thenReturn("C");

        assertRenderingResult("The names: A and X and C", context);
    }

    @Test
    void testDefaultRenderingBypassesAttributeRendererForText()
    {
        Template template = getTemplateAttachedToGroup("begin <x> end",
            loader.getGroup()
                .blank()
                .registerAttributeRenderer(String.class, TestRenderers::toUpperCaseRenderer, NON_RECURSIVE)
                .build());

        assertSingleArgRenderingResult("begin ATTRIBUTE end", template, "x", "attribute");
    }

    @Test
    void testLegacyRenderingUsesAttributeRendererForText()
    {
        Template template = getTemplateAttachedToGroup("begin <x> end",
            loader.getGroup()
                .blank()
                .registerAttributeRenderer(String.class, TestRenderers::toUpperCaseRenderer, NON_RECURSIVE)
                .withLegacyRendering()
                .build());

        assertSingleArgRenderingResult("BEGIN ATTRIBUTE END", template, "x", "attribute");
    }

    @Test
    void testOrderFreeOptions()
    {
        String templates = "foo(x) ::= << begin <x> end >>\n";

        Group group;

        group = loader.getGroup()
            .fromString(templates)
            .withDelimiters('<', '>')
            .withLegacyRendering()
            .withErrorListener(new ErrorBuffer())
            .build();

        group = loader.getGroup()
            .fromString(templates)
            .withDelimiters('<', '>')
            .withErrorListener(new ErrorBuffer())
            .withLegacyRendering()
            .build();

        group = loader.getGroup()
            .fromString(templates)
            .withLegacyRendering()
            .withDelimiters('<', '>')
            .withErrorListener(new ErrorBuffer())
            .build();

        group = loader.getGroup()
            .fromString(templates)
            .withLegacyRendering()
            .withErrorListener(new ErrorBuffer())
            .withDelimiters('<', '>')
            .build();

        group = loader.getGroup()
            .fromString(templates)
            .withErrorListener(new ErrorBuffer())
            .withDelimiters('<', '>')
            .withLegacyRendering()
            .build();

        group = loader.getGroup()
            .fromString(templates)
            .withErrorListener(new ErrorBuffer())
            .withLegacyRendering()
            .withDelimiters('<', '>')
            .build();
    }

    @Test
    void testOrderFreeOptions2()
    {
        String templates = "foo(x) ::= << begin <x> end >>\n";

        Group blankGroup = loader.getGroup()
            .blank()
            .build();

        Group group;

        group = loader.getGroup()
            .fromString(templates)
            .registerModelAdaptor(String.class, MODEL_ADAPTOR)
            .importTemplates(blankGroup)
            .withDelimiters('<', '>')
            .withLegacyRendering()
            .importTemplates(blankGroup)
            .withErrorListener(new ErrorBuffer())
            .registerModelAdaptor(String.class, MODEL_ADAPTOR)
            .build();

        group = loader.getGroup()
            .fromString(templates)
            .withDelimiters('<', '>')
            .importTemplates(blankGroup)
            .importTemplates(blankGroup)
            .withLegacyRendering()
            .withErrorListener(new ErrorBuffer())
            .registerModelAdaptor(String.class, MODEL_ADAPTOR)
            .registerModelAdaptor(String.class, MODEL_ADAPTOR)
            .build();
    }
}
