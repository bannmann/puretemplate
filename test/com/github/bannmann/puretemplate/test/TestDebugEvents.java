package com.github.bannmann.puretemplate.test;

import org.junit.Test;
import com.github.bannmann.puretemplate.*;

import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.STGroupFile;
import com.github.bannmann.puretemplate.STGroupString;
import com.github.bannmann.puretemplate.debug.InterpEvent;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestDebugEvents extends BaseTest {
    @Test public void testString() throws Exception {
        String templates =
            "t() ::= <<foo>>" + newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");
        ST st = group.getInstanceOf("t");
        List<InterpEvent> events = st.getEvents();
        String expected =
            "[EvalExprEvent{self=/t(), expr='foo', exprStartChar=0, exprStopChar=2, start=0, stop=2}," +
            " EvalTemplateEvent{self=/t(), start=0, stop=2}]";
        String result = events.toString();
        assertEquals(expected, result);
    }

    @Test public void testAttribute() throws Exception {
        String templates =
            "t(x) ::= << <x> >>" + newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");
        ST st = group.getInstanceOf("t");
        List<InterpEvent> events = st.getEvents();
        String expected =
            "[IndentEvent{self=/t(), expr=' ', exprStartChar=0, exprStopChar=0, start=0, stop=0}," +
            " EvalExprEvent{self=/t(), expr='<x>', exprStartChar=1, exprStopChar=3, start=0, stop=-1}," +
            " EvalExprEvent{self=/t(), expr=' ', exprStartChar=4, exprStopChar=4, start=0, stop=0}," +
            " EvalTemplateEvent{self=/t(), start=0, stop=0}]";
        String result = events.toString();
        assertEquals(expected, result);
    }

    @Test public void testTemplateCall() throws Exception {
        String templates =
            "t(x) ::= <<[<u()>]>>\n" +
            "u() ::= << <x> >>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");
        ST st = group.getInstanceOf("t");
        group.getInstanceOf("u").impl.dump();
        List<InterpEvent> events = st.getEvents();
        String expected =
            "[EvalExprEvent{self=/t(), expr='[', exprStartChar=0, exprStopChar=0, start=0, stop=0}," +
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
    public void testEvalExprEventForSpecialCharacter() throws Exception {
        String templates = "t() ::= <<[<\\n>]>>\n";
        //                            012 345
        // Rendering t() emits: "[\n]"  or  "[\r\n]" (depends on line.separator)
        //                       01 2        01 2 3
        STGroupString g = new STGroupString(templates);
        ST st = g.getInstanceOf("t");
        st.impl.dump();
        List<InterpEvent> events = st.getEvents();
        int n = newline.length();
        String expected =
            "[EvalExprEvent{self=/t(), expr='[', exprStartChar=0, exprStopChar=0, start=0, stop=0}, " +
            "EvalExprEvent{self=/t(), expr='\\n', exprStartChar=2, exprStopChar=3, start=1, stop="+n+"}, " +
            "EvalExprEvent{self=/t(), expr=']', exprStartChar=5, exprStopChar=5, start="+(n+1)+", stop="+(n+1)+"}, " +
            "EvalTemplateEvent{self=/t(), start=0, stop="+(n+1)+"}]";
        String result = events.toString();
        assertEquals(expected, result);
    }
}
