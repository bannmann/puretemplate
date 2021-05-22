package com.github.bannmann.puretemplate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.bannmann.puretemplate.misc.ErrorBuffer;
import com.github.bannmann.puretemplate.misc.Misc;

public class TestInterptimeErrors extends BaseTest
{
    public static class UserHiddenName
    {
        protected String name;

        public UserHiddenName(String name)
        {
            this.name = name;
        }

        protected String getName()
        {
            return name;
        }
    }

    public static class UserHiddenNameField
    {
        protected String name;

        public UserHiddenNameField(String name)
        {
            this.name = name;
        }
    }

    @Test
    public void testMissingEmbeddedTemplate() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<foo()>\"" + Misc.newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.render();
        String expected = "context [/t] 1:1 no such template: /foo" + newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    public void testMissingSuperTemplate() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<super.t()>\"" + Misc.newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        String templates2 = "u() ::= \"blech\"" + Misc.newline;

        writeFile(tmpdir, "t2.stg", templates2);
        STGroup group2 = new STGroupFile(tmpdir + "/" + "t2.stg");
        group.importTemplates(group2);
        ST st = group.getInstanceOf("t");
        st.render();
        String expected = "context [/t] 1:1 no such template: super.t" + newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    public void testNoPropertyNotError() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t(u) ::= \"<u.x>\"" + Misc.newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.add("u", new User(32, "parrt"));
        st.render();
        String expected = "";
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    public void testHiddenPropertyNotError() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t(u) ::= \"<u.name>\"" + Misc.newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.add("u", new UserHiddenName("parrt"));
        st.render();
        String expected = "";
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    public void testHiddenFieldNotError() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t(u) ::= \"<u.name>\"" + Misc.newline;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.add("u", new UserHiddenNameField("parrt"));
        st.render();
        String expected = "";
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    public void testSoleArg() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<u({9})>\"\n" + "u(x,y) ::= \"<x>\"\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.render();
        String expected = "context [/t] 1:1 passed 1 arg(s) to template /u with 2 declared arg(s)" + newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    public void testSoleArgUsingApplySyntax() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<{9}:u()>\"\n" + "u(x,y) ::= \"<x>\"\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        String expected = "9";
        String result = st.render();
        assertEquals(expected, result);

        expected = "context [/t] 1:5 passed 1 arg(s) to template /u with 2 declared arg(s)" + newline;
        result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    public void testUndefinedAttr() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<u()>\"\n" + "u() ::= \"<x>\"\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.render();
        String expected = "context [/t /u] 1:1 attribute x isn't defined" + newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    public void testParallelAttributeIterationWithMissingArgs() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new STGroup();
        group.setListener(errors);
        ST e = new ST(group, "<names,phones,salaries:{n,p | <n>@<p>}; separator=\", \">");
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        e.add("salaries", "big");
        e.render();
        String errorExpecting = "1:23: anonymous template has 2 arg(s) but mapped across 3 value(s)" +
            newline +
            "context [anonymous] 1:23 passed 3 arg(s) to template /_sub1 with 2 declared arg(s)" +
            newline +
            "context [anonymous] 1:1 iterating through 3 values in zip map but template has 2 declared arguments" +
            newline;
        assertEquals(errorExpecting, errors.toString());
        String expecting = "Ter@1, Tom@2";
        assertEquals(expecting, e.render());
    }

    @Test
    public void testStringTypeMismatch() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new STGroup();
        group.setListener(errors);
        ST e = new ST(group, "<trim(s)>");
        e.add("s", 34);
        e.render(); // generate the error
        String errorExpecting = "context [anonymous] 1:1 function trim expects a string not java.lang.Integer" +
            newline;
        assertEquals(errorExpecting, errors.toString());
    }

    @Test
    public void testStringTypeMismatch2() throws Exception
    {
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new STGroup();
        group.setListener(errors);
        ST e = new ST(group, "<strlen(s)>");
        e.add("s", 34);
        e.render(); // generate the error
        String errorExpecting = "context [anonymous] 1:1 function strlen expects a string not java.lang.Integer" +
            newline;
        assertEquals(errorExpecting, errors.toString());
    }
}
