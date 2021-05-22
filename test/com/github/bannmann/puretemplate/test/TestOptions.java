package com.github.bannmann.puretemplate.test;

import org.junit.*;

import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.misc.ErrorBuffer;

import static org.junit.Assert.assertEquals;

public class TestOptions extends BaseTest {
    @Test public void testSeparator() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name", "hi <name; separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected = "hi Ter, Tom, Sumana!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testSeparatorWithSpaces() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name", "hi <name; separator= \", \">!");
        ST st = group.getInstanceOf("test");
        System.out.println(st.impl.ast.toStringTree());
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected = "hi Ter, Tom, Sumana!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testAttrSeparator() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name,sep", "hi <name; separator=sep>!");
        ST st = group.getInstanceOf("test");
        st.add("sep", ", ");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected = "hi Ter, Tom, Sumana!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testIncludeSeparator() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("foo", "|");
        group.defineTemplate("test", "name,sep", "hi <name; separator=foo()>!");
        ST st = group.getInstanceOf("test");
        st.add("sep", ", ");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected = "hi Ter|Tom|Sumana!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testSubtemplateSeparator() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name,sep", "hi <name; separator={<sep> _}>!");
        ST st = group.getInstanceOf("test");
        st.add("sep", ",");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected = "hi Ter, _Tom, _Sumana!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testSeparatorWithNullFirstValueAndNullOption() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name", "hi <name; null=\"n/a\", separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.add("name", null);
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected = "hi n/a, Tom, Sumana!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testSeparatorWithNull2ndValueAndNullOption() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name", "hi <name; null=\"n/a\", separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.impl.dump();
        st.add("name", "Ter");
        st.add("name", null);
        st.add("name", "Sumana");
        String expected = "hi Ter, n/a, Sumana!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testNullValueAndNullOption() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name", "<name; null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        st.add("name", null);
        String expected = "n/a";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testListApplyWithNullValueAndNullOption() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name", "<name:{n | <n>}; null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", null);
        st.add("name", "Sumana");
        String expected = "Tern/aSumana";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testDoubleListApplyWithNullValueAndNullOption() throws Exception {
        // first apply sends [ST, null, ST] to second apply, which puts [] around
        // the value.  This verifies that null not blank comes out of first apply
        // since we don't get [null].
        STGroup group = new STGroup();
        group.defineTemplate("test", "name", "<name:{n | <n>}:{n | [<n>]}; null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", null);
        st.add("name", "Sumana");
        String expected = "[Ter]n/a[Sumana]";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testMissingValueAndNullOption() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "name", "<name; null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        String expected = "n/a";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testOptionDoesntApplyToNestedTemplate() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("foo", "<zippo>");
        group.defineTemplate("test", "zippo", "<foo(); null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        st.add("zippo", null);
        String expected = "";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testIllegalOption() throws Exception {
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new STGroup();
        group.setListener(errors);
        group.defineTemplate("test", "name", "<name; bad=\"ugly\">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        String expected = "Ter";
        String result = st.render();
        assertEquals(expected, result);
        expected = "[test 1:7: no such option: bad]";
        assertEquals(expected, errors.errors.toString());
    }
}
