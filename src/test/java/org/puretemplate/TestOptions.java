package org.puretemplate;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.puretemplate.misc.ErrorBuffer;

public class TestOptions extends BaseTest
{
    @Test
    public void testSeparator() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi Ter, Tom, Sumana!", st);
    }

    @Test
    public void testSeparatorWithSpaces() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; separator= \", \">!");
        ST st = group.getInstanceOf("test");
        System.out.println(st.impl.ast.toStringTree());
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi Ter, Tom, Sumana!", st);
    }

    @Test
    public void testAttrSeparator() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name,sep", "hi <name; separator=sep>!");
        ST st = group.getInstanceOf("test");
        st.add("sep", ", ");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi Ter, Tom, Sumana!", st);
    }

    @Test
    public void testIncludeSeparator() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("foo", "|");
        group.defineTemplate("test", "name,sep", "hi <name; separator=foo()>!");
        ST st = group.getInstanceOf("test");
        st.add("sep", ", ");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi Ter|Tom|Sumana!", st);
    }

    @Test
    public void testSubtemplateSeparator() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name,sep", "hi <name; separator={<sep> _}>!");
        ST st = group.getInstanceOf("test");
        st.add("sep", ",");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi Ter, _Tom, _Sumana!", st);
    }

    @Test
    public void testSeparatorWithNullFirstValueAndNullOption() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; null=\"n/a\", separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.add("name", null);
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi n/a, Tom, Sumana!", st);
    }

    @Test
    public void testSeparatorWithNull2ndValueAndNullOption() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; null=\"n/a\", separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.impl.dump();
        st.add("name", "Ter");
        st.add("name", null);
        st.add("name", "Sumana");
        assertRenderingResult("hi Ter, n/a, Sumana!", st);
    }

    @Test
    public void testNullValueAndNullOption() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name; null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        st.add("name", null);
        assertRenderingResult("n/a", st);
    }

    @Test
    public void testListApplyWithNullValueAndNullOption() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name:{n | <n>}; null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", null);
        st.add("name", "Sumana");
        assertRenderingResult("Tern/aSumana", st);
    }

    @Test
    public void testDoubleListApplyWithNullValueAndNullOption() throws IOException
    {
        // first apply sends [ST, null, ST] to second apply, which puts [] around
        // the value.  This verifies that null not blank comes out of first apply
        // since we don't get [null].
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name:{n | <n>}:{n | [<n>]}; null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", null);
        st.add("name", "Sumana");
        assertRenderingResult("[Ter]n/a[Sumana]", st);
    }

    @Test
    public void testMissingValueAndNullOption() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name; null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        assertRenderingResult("n/a", st);
    }

    @Test
    public void testOptionDoesntApplyToNestedTemplate() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("foo", "<zippo>");
        group.defineTemplate("test", "zippo", "<foo(); null=\"n/a\">");
        ST st = group.getInstanceOf("test");
        st.add("zippo", null);
        assertRenderingResult("", st);
    }

    @Test
    public void testIllegalOption() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new LegacyBareStGroup();
        group.setListener(errors);
        group.defineTemplate("test", "name", "<name; bad=\"ugly\">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        assertRenderingResult("Ter", st);
        assertEquals("[test 1:7: no such option: bad]",
            errors.getErrors()
                .toString());
    }
}
