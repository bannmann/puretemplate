package com.example;

import static java.util.Calendar.JULY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.puretemplate.Loader.RendererDepth.NON_RECURSIVE;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.Template;
import org.puretemplate.misc.ErrorBuffer;
import org.puretemplate.model.AttributeRenderer;
import org.puretemplate.model.ModelAdaptor;
import org.puretemplate.model.NumberRenderer;
import org.puretemplate.model.ObjectTypedDateRenderer;
import org.puretemplate.model.StringRenderer;

import com.google.common.collect.Lists;

class TestRenderers extends BaseTest
{
    private static final GregorianCalendar CALENDAR_2005_07_05 = new GregorianCalendar(2005, JULY, 5);
    private static final ZoneId LOS_ANGELES = ZoneId.of("America/Los_Angeles");
    public static final ModelAdaptor<? super String> MODEL_ADAPTOR = (model, property, propertyName) -> "yay";

    private static GregorianCalendar withZoneSameLocal(GregorianCalendar input, ZoneId zone)
    {
        return GregorianCalendar.from(input.toZonedDateTime()
            .withZoneSameLocal(zone));
    }

    private static String toUpperCaseRenderer(String value, String formatString, Locale locale)
    {
        return value.toUpperCase();
    }

    @Test
    void testRendererWithSystemDefaultLocale()
    {
        String templates = "test(arg) ::= \"<arg>\"\n";

        AttributeRenderer<Number> mockRenderer = mockAttributeRenderer(Number.class);

        Locale originalDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
        try
        {
            Context context = loader.getGroup()
                .fromString(templates)
                .registerAttributeRenderer(Number.class, mockRenderer, NON_RECURSIVE)
                .build()
                .getTemplate("test")
                .createContext()
                .useSystemDefaultLocale()
                .add("arg", 1337);

            context.render()
                .intoString();

            verify(mockRenderer).render(1337, null, Locale.GERMANY);
        }
        finally
        {
            Locale.setDefault(originalDefaultLocale);
        }
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private <T> AttributeRenderer<T> mockAttributeRenderer(Class<T> attributeClass)
    {
        AttributeRenderer<T> mock = Mockito.mock(AttributeRenderer.class);
        when(mock.render(any(), any(), any(Locale.class))).thenReturn("");
        return mock;
    }

    @Test
    void testRendererForGroup()
    {
        String templates = "dateThing(created) ::= \"datetime: <created>\"\n";

        Context context = getGroupWithDateRenderer(templates).getTemplate("dateThing")
            .createContext()
            .setLocale(Locale.US)
            .add("created", CALENDAR_2005_07_05);

        String expecting = "datetime: 7/5/05, 12:00 AM";

        assertRenderingResult(expecting, context);
    }

    private Group getGroupWithDateRenderer(String templates)
    {
        return loader.getGroup()
            .fromString(templates)
            .registerAttributeRenderer(GregorianCalendar.class, new ObjectTypedDateRenderer(), NON_RECURSIVE)
            .build();
    }

    @Test
    void testRendererWithFormat()
    {
        String templates = "dateThing(created) ::= << date: <created; format=\"yyyy.MM.dd\"> >>\n";

        Context context = getGroupWithDateRenderer(templates).getTemplate("dateThing")
            .createContext()
            .add("created", CALENDAR_2005_07_05);

        assertRenderingResult(" date: 2005.07.05 ", context);
    }

    @Test
    void testRendererWithPredefinedFormat()
    {
        String templates = "dateThing(created) ::= << datetime: <created; format=\"short\"> >>\n";

        Context context = getGroupWithDateRenderer(templates).getTemplate("dateThing")
            .createContext()
            .setLocale(Locale.US)
            .add("created", CALENDAR_2005_07_05);

        assertRenderingResult(" datetime: 7/5/05, 12:00 AM ", context);
    }

    @Test
    void testRendererWithPredefinedFormat2()
    {
        String templates = "dateThing(created) ::= << datetime: <created; format=\"full\"> >>\n";

        Context context = getGroupWithDateRenderer(templates).getTemplate("dateThing")
            .createContext()
            .setLocale(Locale.US)
            .add("created", withZoneSameLocal(CALENDAR_2005_07_05, LOS_ANGELES));

        TimeZone origTimeZone = TimeZone.getDefault();
        try
        {
            // set Timezone to "PDT"
            TimeZone.setDefault(TimeZone.getTimeZone(LOS_ANGELES));

            assertRenderingResult(" datetime: Tuesday, July 5, 2005 at 12:00:00 AM Pacific Daylight Time ", context);
        }
        finally
        {
            // Restore original Timezone
            TimeZone.setDefault(origTimeZone);
        }
    }

    @Test
    void testRendererWithPredefinedFormat3()
    {
        String templates = "dateThing(created) ::= << date: <created; format=\"date:medium\"> >>\n";

        Context context = getGroupWithDateRenderer(templates).getTemplate("dateThing")
            .createContext()
            .setLocale(Locale.US)
            .add("created", CALENDAR_2005_07_05);

        assertRenderingResult(" date: Jul 5, 2005 ", context);
    }

    @Test
    void testRendererWithPredefinedFormat4()
    {
        String templates = "dateThing(created) ::= << time: <created; format=\"time:medium\"> >>\n";

        Context context = getGroupWithDateRenderer(templates).getTemplate("dateThing")
            .createContext()
            .setLocale(Locale.US)
            .add("created", CALENDAR_2005_07_05);

        assertRenderingResult(" time: 12:00:00 AM ", context);
    }

    @Test
    void testStringRendererWithFormat_cap()
    {
        String templates = "foo(x) ::= << <x; format=\"cap\"> >>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", "hi");

        assertRenderingResult(" Hi ", context);
    }

    public Group getGroupWithStringRenderer(String templates)
    {
        return loader.getGroup()
            .fromString(templates)
            .registerAttributeRenderer(String.class, new StringRenderer(), NON_RECURSIVE)
            .build();
    }

    @Test
    void testStringRendererWithTemplateInclude_cap()
    {
        // must toString the t() ref before applying format
        String templates = "foo(x) ::= << <(t()); format=\"cap\"> >>\n" + "t() ::= <<ack>>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", "hi");

        assertRenderingResult(" Ack ", context);
    }

    @Test
    void testStringRendererWithSubtemplateInclude_cap()
    {
        String templates = "foo(x) ::= << <({ack}); format=\"cap\"> >>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", "hi");

        assertRenderingResult(" Ack ", context);
    }

    @Test
    void testStringRendererWithFormat_cap_emptyValue()
    {
        String templates = "foo(x) ::= << <x; format=\"cap\"> >>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", "");

        assertRenderingResult(" ", context); //FIXME: why not two spaces?
    }

    @Test
    void testStringRendererWithFormat_url_encode()
    {
        String templates = "foo(x) ::= << <x; format=\"url-encode\"> >>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", "a b");

        assertRenderingResult(" a+b ", context);
    }

    @Test
    void testStringRendererWithFormat_xml_encode()
    {
        String templates = "foo(x) ::= << <x; format=\"xml-encode\"> >>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", "a<b> &\t\b");

        assertRenderingResult(" a&lt;b&gt; &amp;\t&#8; ", context);
    }

    @Test
    void testStringRendererWithFormat_xml_encode_null()
    {
        String templates = "foo(x) ::= << <x; format=\"xml-encode\"> >>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", null);

        assertRenderingResult(" ", context);
    }

    @Test
    void testStringRendererWithFormat_xml_encode_emoji()
    {
        String templates = "foo(x) ::= << <x; format=\"xml-encode\"> >>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", "\uD83E\uDE73");

        assertRenderingResult(" &#129651; ", context);
    }

    @Test
    void testStringRendererWithPrintfFormat()
    {
        String templates = "foo(x) ::= << <x; format=\"%6s\"> >>\n";

        Context context = getGroupWithStringRenderer(templates).getTemplate("foo")
            .createContext()
            .add("x", "hi");

        assertRenderingResult("     hi ", context);
    }

    @Test
    void testNumberRendererWithPrintfFormat()
    {
        String templates = "foo(x,y) ::= << <x; format=\"%d\"> <y; format=\"%2.3f\"> >>\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .registerAttributeRenderer(Integer.class, new NumberRenderer(), NON_RECURSIVE)
            .registerAttributeRenderer(Double.class, new NumberRenderer(), NON_RECURSIVE)
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", -2100)
            .add("y", 3.14159);

        assertRenderingResult(" -2100 3.142 ", context);
    }

    @Test
    void testInstanceofRenderer()
    {
        String templates = "numberThing(x,y,z) ::= \"numbers: <x>, <y>; <z>\"\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .registerAttributeRenderer(Number.class, new NumberRenderer(), NON_RECURSIVE)
            .build()
            .getTemplate("numberThing")
            .createContext()
            .add("x", -2100)
            .add("y", 3.14159)
            .add("z", "hi");

        assertRenderingResult("numbers: -2100, 3.14159; hi", context);
    }

    @Test
    void testLocaleWithNumberRenderer()
    {
        String templates = "foo(x,y) ::= <<\n" + "<x; format=\"%,d\"> <y; format=\"%,2.3f\">\n" + ">>\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .registerAttributeRenderer(Number.class, new NumberRenderer(), NON_RECURSIVE)
            .registerAttributeRenderer(Double.class, new NumberRenderer(), NON_RECURSIVE)
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", -2100)
            .add("y", 3.14159)
            .setLocale(new Locale("pl"));

        // Polish uses ' ' (ASCII 160) for ',' and ',' for '.'
        assertRenderingResult("-2\u00A0100 3,142", context);
    }

    @Test
    void testRendererWithFormatAndList()
    {
        String template = "The names: <names; format=\"upper\">";

        Context context = getTemplateWithStringRenderer(template).createContext()
            .add("names", "ter")
            .add("names", "tom")
            .add("names", "sriram");

        assertRenderingResult("The names: TERTOMSRIRAM", context);
    }

    private Template getTemplateWithStringRenderer(String template)
    {
        Group group = loader.getGroup()
            .blank()
            .registerAttributeRenderer(String.class, new StringRenderer(), NON_RECURSIVE)
            .build();

        return loader.getTemplate()
            .fromString(template)
            .attachedToGroup(group)
            .build();
    }

    @Test
    void testRendererWithFormatAndSeparator()
    {
        String template = "The names: <names; separator=\" and \", format=\"upper\">";

        Context context = getTemplateWithStringRenderer(template).createContext()
            .add("names", "ter")
            .add("names", "tom")
            .add("names", "sriram");

        assertRenderingResult("The names: TER and TOM and SRIRAM", context);
    }

    @Test
    void testRendererWithFormatAndSeparatorAndNull()
    {
        String template = "The names: <names; separator=\" and \", null=\"n/a\", format=\"upper\">";

        Context context = getTemplateWithStringRenderer(template).createContext()
            .add("names", Lists.newArrayList("ter", null, "sriram"));

        assertRenderingResult("The names: TER and N/A and SRIRAM", context);
    }

    @Test
    void testDateRendererWithLocale()
    {
        String template = "<date; format=\"dd 'de' MMMMM 'de' yyyy\">";

        Group group = loader.getGroup()
            .blank()
            .registerAttributeRenderer(Calendar.class, new ObjectTypedDateRenderer(), NON_RECURSIVE)
            .build();

        Context context = loader.getTemplate()
            .fromString(template)
            .attachedToGroup(group)
            .build()
            .createContext()
            .setLocale(new Locale("pt"));

        Calendar cal = Calendar.getInstance();
        cal.set(2012, Calendar.JUNE, 12);
        context.add("date", cal);

        assertRenderingResult("12 de junho de 2012", context);
    }

    @Test
    void testDefaultRenderingBypassesAttributeRendererForText()
    {
        String templates = "foo(x) ::= << begin <x> end >>\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .registerAttributeRenderer(String.class, TestRenderers::toUpperCaseRenderer, NON_RECURSIVE)
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", "attribute");

        assertRenderingResult(" begin ATTRIBUTE end ", context);
    }

    @Test
    void testLegacyRenderingUsesAttributeRendererForText()
    {
        String templates = "foo(x) ::= << begin <x> end >>\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .withLegacyRendering()
            .registerAttributeRenderer(String.class, TestRenderers::toUpperCaseRenderer, NON_RECURSIVE)
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", "attribute");

        assertRenderingResult(" BEGIN ATTRIBUTE END ", context);
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
