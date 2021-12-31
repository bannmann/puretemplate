package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.Template;
import org.puretemplate.diagnostics.ContextDiagnostics;
import org.puretemplate.diagnostics.Event;
import org.puretemplate.diagnostics.EventListener;

@Slf4j
class TestRenderingEvents extends BaseTest
{
    public static class EventBuffer implements EventListener
    {
        private final List<Event> events = new ArrayList<>();

        public <E extends Event & Event.DistributionTarget> void addTo(
            ContextDiagnostics diagnostics, List<Class<E>> eventInterfaces)
        {
            eventInterfaces.forEach(eClass -> diagnostics.addEventListener(this, eClass));
        }

        @Override
        public void onEvalExpression(Event.EvalExpression event)
        {
            events.add(event);
        }

        @Override
        public void onEvalTemplate(Event.EvalTemplate event)
        {
            events.add(event);
        }

        @Override
        public void onIndent(Event.Indent event)
        {
            events.add(event);
        }

        @Override
        public void onTrace(Event.Trace event)
        {
            events.add(event);
        }

        public List<Event> getEvents()
        {
            return Collections.unmodifiableList(events);
        }
    }

    private static <E extends Event & Event.DistributionTarget> List<Class<E>> usingStandardEventInterfaces()
    {
        return List.of(cast(Event.EvalExpression.class), cast(Event.EvalTemplate.class), cast(Event.Indent.class));
    }

    private static <E extends Event & Event.DistributionTarget> List<Class<E>> usingTraceEventInterface()
    {
        return List.of(cast(Event.Trace.class));
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> cast(Class<?> raw)
    {
        return (Class<T>) raw;
    }

    @Test
    void testString()
    {
        Group group = loadGroupFromString("t() ::= <<foo>>" + NEWLINE);
        List<String> expectedEvents = List.of(
            "EvalExpression{location=/t#1:0, expr='foo', exprStartChar=0, exprStopChar=2, start=0, stop=2}",
            "EvalTemplate{location=/t#1:0, start=0, stop=2}");
        assertRenderingEvents(expectedEvents, group, "t", usingStandardEventInterfaces());
    }

    private <E extends Event & Event.DistributionTarget> void assertRenderingEvents(
        List<String> expectedEvents,
        Group group,
        @SuppressWarnings("SameParameterValue") String templateName,
        List<Class<E>> eventInterfaces)
    {
        Template template = group.getTemplate(templateName);
        List<String> events = renderAndGetEvents(template, eventInterfaces).stream()
            .map(Objects::toString)
            .collect(Collectors.toList());
        assertThat(events).containsExactlyElementsOf(expectedEvents);
    }

    private <E extends Event & Event.DistributionTarget> List<Event> renderAndGetEvents(
        Template template, List<Class<E>> eventInterfaces)
    {
        Context context = template.createContext();

        EventBuffer eventBuffer = new EventBuffer();
        eventBuffer.addTo(context.diagnostics(), eventInterfaces);

        context.render()
            .intoString();

        return eventBuffer.getEvents();
    }

    @Test
    void testAttribute()
    {
        Group group = loadGroupFromString("t(x) ::= << <x> >>" + NEWLINE);
        List<String> expectedEvents = List.of(
            "Indent{location=/t#1:0, expr=' ', exprStartChar=0, exprStopChar=0, start=0, stop=0}",
            "EvalExpression{location=/t#1:1, expr='<x>', exprStartChar=1, exprStopChar=3, start=0, stop=-1}",
            "EvalExpression{location=/t#1:4, expr=' ', exprStartChar=4, exprStopChar=4, start=0, stop=0}",
            "EvalTemplate{location=/t#1:4, start=0, stop=0}");
        assertRenderingEvents(expectedEvents, group, "t", usingStandardEventInterfaces());
    }

    @Test
    void testAttributeTrace()
    {
        Group group = loadGroupFromString("t(x) ::= << <x> >>" + NEWLINE);
        List<String> expectedEvents = List.of(
            "Trace{/t#0000: indent #0:\" \" | stack=[], coordinates=1:0, stackPointer=-1, currentLineCharacters=0}",
            "Trace{/t#0003: load_local 0 | stack=[], coordinates=1:0, stackPointer=-1, currentLineCharacters=0}",
            "Trace{/t#0006: write | stack=[null], coordinates=1:2, stackPointer=0, currentLineCharacters=0}",
            "Trace{/t#0007: dedent | stack=[], coordinates=1:1, stackPointer=-1, currentLineCharacters=0}",
            "Trace{/t#0008: write_str #0:\" \" | stack=[], coordinates=null, stackPointer=-1, currentLineCharacters=0}");

        assertRenderingEvents(expectedEvents, group, "t", usingTraceEventInterface());
    }

    @Test
    void testInstructions()
    {
        Group group = loadGroupFromString("t(x) ::= << <x> >>" + NEWLINE);
        String statements = group.getTemplate("t")
            .diagnostics()
            .getStatementsAsString();

        assertThat(statements).isEqualTo("indent 0, load_local 0, write, dedent, write_str 0");
    }

    @Test
    void testGetDump()
    {
        Group group = loadGroupFromString("t(x) ::= << <x> >>" + NEWLINE);
        String dump = group.getTemplate("t")
            .diagnostics()
            .getDump();

        assertThat(dump).isEqualToNormalizingNewlines("/t:\n" +
            "0000:\tindent        #0:\" \"\n" +
            "0003:\tload_local    0\n" +
            "0006:\twrite           \n" +
            "0007:\tdedent          \n" +
            "0008:\twrite_str     #0:\" \"\n" +
            "\n" +
            "Strings:\n" +
            "0000: \" \"\n" +
            "\n" +
            "Bytecode to template map:\n" +
            "0000: 0..0\t\" \"\n" +
            "0003: 2..2\t\"x\"\n" +
            "0006: 1..3\t\"<x>\"\n" +
            "0008: 4..4\t\" \"\n" +
            "\n");
    }

    @Test
    void testTemplateCall()
    {
        Group group = loadGroupFromString("t(x) ::= <<[<u()>]>>\n" + "u() ::= << <x> >>\n");
        List<String> expectedEvents = List.of(
            "EvalExpression{location=/t#1:0, expr='[', exprStartChar=0, exprStopChar=0, start=0, stop=0}",
            "Indent{location=/u#1:0, expr=' ', exprStartChar=0, exprStopChar=0, start=1, stop=1}",
            "EvalExpression{location=/u#1:1, expr='<x>', exprStartChar=1, exprStopChar=3, start=1, stop=0}",
            "EvalExpression{location=/u#1:4, expr=' ', exprStartChar=4, exprStopChar=4, start=1, stop=1}",
            "EvalTemplate{location=/u#1:4, start=1, stop=1}",
            "EvalExpression{location=/t#1:1, expr='<u()>', exprStartChar=1, exprStopChar=5, start=1, stop=1}",
            "EvalExpression{location=/t#1:6, expr=']', exprStartChar=6, exprStopChar=6, start=2, stop=2}",
            "EvalTemplate{location=/t#1:6, start=0, stop=2}");
        assertRenderingEvents(expectedEvents, group, "t", usingStandardEventInterfaces());
    }

    @Test
    void testEvalExpressionEventForSpecialCharacter()
    {
        Group group = loadGroupFromString("t() ::= <<[<\\n>]>>\n");
        //                                           012 345
        // Rendering t() emits: "[\n]"  or  "[\r\n]" (depends on line.separator)
        //                       01 2        01 2 3
        int n = NEWLINE.length();
        List<String> expectedEvents = List.of(
            "EvalExpression{location=/t#1:0, expr='[', exprStartChar=0, exprStopChar=0, start=0, stop=0}",
            "EvalExpression{location=/t#1:2, expr='\\n', exprStartChar=2, exprStopChar=3, start=1, stop=" + n + "}",
            "EvalExpression{location=/t#1:5, expr=']', exprStartChar=5, exprStopChar=5, start=" +
                (n + 1) +
                ", stop=" +
                (n + 1) +
                "}",
            "EvalTemplate{location=/t#1:5, start=0, stop=" + (n + 1) + "}");
        assertRenderingEvents(expectedEvents, group, "t", usingStandardEventInterfaces());
    }
}
