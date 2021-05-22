package com.github.bannmann.puretemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.bannmann.puretemplate.misc.ErrorBuffer;

public class TestIndirectionAndEarlyEval extends BaseTest
{
    @Test
    public void testEarlyEval() throws Exception
    {
        String template = "<(name)>";
        ST st = new ST(template);
        st.add("name", "Ter");
        String expected = "Ter";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testIndirectTemplateInclude() throws Exception
    {
        STGroup group = new STGroup();
        group.defineTemplate("foo", "bar");
        String template = "<(name)()>";
        group.defineTemplate("test", "name", template);
        ST st = group.getInstanceOf("test");
        st.add("name", "foo");
        String expected = "bar";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testIndirectTemplateIncludeWithArgs() throws Exception
    {
        STGroup group = new STGroup();
        group.defineTemplate("foo", "x,y", "<x><y>");
        String template = "<(name)({1},{2})>";
        group.defineTemplate("test", "name", template);
        ST st = group.getInstanceOf("test");
        st.add("name", "foo");
        String expected = "12";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testIndirectCallWithPassThru() throws Exception
    {
        // pass-through for dynamic template invocation is not supported by the
        // bytecode representation
        writeFile(tmpdir,
            "t.stg",
            "t1(x) ::= \"<x>\"\n" + "main(x=\"hello\",t=\"t1\") ::= <<\n" + "<(t)(...)>\n" + ">>");
        STGroup group = new STGroupFile(tmpdir + "/t.stg");
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        ST st = group.getInstanceOf("main");
        assertEquals("t.stg 2:34: mismatched input '...' expecting RPAREN" + newline, errors.toString());
        assertNull(st);
    }

    @Test
    public void testIndirectTemplateIncludeViaTemplate() throws Exception
    {
        STGroup group = new STGroup();
        group.defineTemplate("foo", "bar");
        group.defineTemplate("tname", "foo");
        String template = "<(tname())()>";
        group.defineTemplate("test", "name", template);
        ST st = group.getInstanceOf("test");
        String expected = "bar";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testIndirectProp() throws Exception
    {
        String template = "<u.(propname)>: <u.name>";
        ST st = new ST(template);
        st.add("u", new TestCoreBasics.User(1, "parrt"));
        st.add("propname", "id");
        String expected = "1: parrt";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testIndirectMap() throws Exception
    {
        STGroup group = new STGroup();
        group.defineTemplate("a", "x", "[<x>]");
        group.defineTemplate("test", "names,templateName", "hi <names:(templateName)()>!");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        st.add("templateName", "a");
        String expected = "hi [Ter][Tom][Sumana]!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test
    public void testNonStringDictLookup() throws Exception
    {
        String template = "<m.(intkey)>";
        ST st = new ST(template);
        Map<Integer, String> m = new HashMap<Integer, String>();
        m.put(36, "foo");
        st.add("m", m);
        st.add("intkey", 36);
        String expected = "foo";
        String result = st.render();
        assertEquals(expected, result);
    }
}
