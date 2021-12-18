package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

@Slf4j
class TestDebugEvents extends BaseTest
{
    @Test
    void testString()
    {
        String templates = "t() ::= <<foo>>" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        ST st = group.getInstanceOf("t");
        List<InterpEvent> events = st.getEvents();
        String expected = "[EvalExprEvent{self=/t(), expr='foo', exprStartChar=0, exprStopChar=2, start=0, stop=2}," +
            " EvalTemplateEvent{self=/t(), start=0, stop=2}]";
        String result = events.toString();
        assertEquals(expected, result);
    }

    @Test
    void testAttribute()
    {
        String templates = "t(x) ::= << <x> >>" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        ST st = group.getInstanceOf("t");
        List<InterpEvent> events = st.getEvents();
        String expected = "[IndentEvent{self=/t(), expr=' ', exprStartChar=0, exprStopChar=0, start=0, stop=0}," +
            " EvalExprEvent{self=/t(), expr='<x>', exprStartChar=1, exprStopChar=3, start=0, stop=-1}," +
            " EvalExprEvent{self=/t(), expr=' ', exprStartChar=4, exprStopChar=4, start=0, stop=0}," +
            " EvalTemplateEvent{self=/t(), start=0, stop=0}]";
        String result = events.toString();
        assertEquals(expected, result);
    }

    @Test
    void testTemplateCall()
    {
        String templates = "t(x) ::= <<[<u()>]>>\n" + "u() ::= << <x> >>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        ST st = group.getInstanceOf("t");
        dump(log, group.getInstanceOf("u").impl);
        List<InterpEvent> events = st.getEvents();
        String expected = "[EvalExprEvent{self=/t(), expr='[', exprStartChar=0, exprStopChar=0, start=0, stop=0}," +
            " IndentEvent{self=/u(), expr=' ', exprStartChar=0, exprStopChar=0, start=1, stop=1}," +
            " EvalExprEvent{self=/u(), expr='<x>', exprStartChar=1, exprStopChar=3, start=1, stop=0}," +
            " EvalExprEvent{self=/u(), expr=' ', exprStartChar=4, exprStopChar=4, start=1, stop=1}," +
            " EvalTemplateEvent{self=/u(), start=1, stop=1}," +
            " EvalExprEvent{self=/t(), expr='<u()>'," +
            " exprStartChar=1, exprStopChar=5, start=1, stop=1}," +
            " EvalExprEvent{self=/t(), expr=']'," +
            " exprStartChar=6, exprStopChar=6, start=2, stop=2}," +
            " EvalTemplateEvent{self=/t(), start=0, stop=2}]";
        String result = events.toString();
        assertEquals(expected, result);
    }

    @Test
    void testEvalExprEventForSpecialCharacter()
    {
        String templates = "t() ::= <<[<\\n>]>>\n";
        //                            012 345
        // Rendering t() emits: "[\n]"  or  "[\r\n]" (depends on line.separator)
        //                       01 2        01 2 3
        STGroupString g = new STGroupString(templates);
        ST st = g.getInstanceOf("t");
        dump(log, st.impl);
        List<InterpEvent> events = st.getEvents();
        int n = NEWLINE.length();
        String expected = "[EvalExprEvent{self=/t(), expr='[', exprStartChar=0, exprStopChar=0, start=0, stop=0}, " +
            "EvalExprEvent{self=/t(), expr='\\n', exprStartChar=2, exprStopChar=3, start=1, stop=" +
            n +
            "}, " +
            "EvalExprEvent{self=/t(), expr=']', exprStartChar=5, exprStopChar=5, start=" +
            (n + 1) +
            ", stop=" +
            (n + 1) +
            "}, " +
            "EvalTemplateEvent{self=/t(), start=0, stop=" +
            (n + 1) +
            "}]";
        String result = events.toString();
        assertEquals(expected, result);
    }
}
