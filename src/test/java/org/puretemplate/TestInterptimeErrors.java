package org.puretemplate;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.puretemplate.misc.ErrorBuffer;

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
    public void testMissingEmbeddedTemplate()
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<foo()>\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.render();
        assertEquals("context [/t] 1:1 no such template: /foo" + NEWLINE, errors.toString());
    }

    @Test
    public void testMissingSuperTemplate()
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<super.t()>\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        String templates2 = "u() ::= \"blech\"" + NEWLINE;

        writeFile(tmpdir, "t2.stg", templates2);
        STGroup group2 = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t2.stg");
        group.importTemplates(group2);
        ST st = group.getInstanceOf("t");
        st.render();
        assertEquals("context [/t] 1:1 no such template: super.t" + NEWLINE, errors.toString());
    }

    @Test
    public void testNoPropertyNotError()
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t(u) ::= \"<u.x>\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.add("u", new User(32, "parrt"));
        st.render();
        assertEquals("", errors.toString());
    }

    @Test
    public void testHiddenPropertyNotError()
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t(u) ::= \"<u.name>\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.add("u", new UserHiddenName("parrt"));
        st.render();
        assertEquals("", errors.toString());
    }

    @Test
    public void testHiddenFieldNotError()
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t(u) ::= \"<u.name>\"" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.add("u", new UserHiddenNameField("parrt"));
        st.render();
        assertEquals("", errors.toString());
    }

    @Test
    public void testSoleArg()
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<u({9})>\"\n" + "u(x,y) ::= \"<x>\"\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.render();
        assertEquals("context [/t] 1:1 passed 1 arg(s) to template /u with 2 declared arg(s)" + NEWLINE,
            errors.toString());
    }

    @Test
    public void testSoleArgUsingApplySyntax() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<{9}:u()>\"\n" + "u(x,y) ::= \"<x>\"\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        assertRenderingResult("9", st);

        assertEquals("context [/t] 1:5 passed 1 arg(s) to template /u with 2 declared arg(s)" + NEWLINE,
            errors.toString());
    }

    @Test
    public void testUndefinedAttr()
    {
        ErrorBuffer errors = new ErrorBuffer();

        String templates = "t() ::= \"<u()>\"\n" + "u() ::= \"<x>\"\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.render();
        assertEquals("context [/t /u] 1:1 attribute x isn't defined" + NEWLINE, errors.toString());
    }

    @Test
    public void testParallelAttributeIterationWithMissingArgs() throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new LegacyBareStGroup();
        group.setListener(errors);
        ST e = new ST(group, "<names,phones,salaries:{n,p | <n>@<p>}; separator=\", \">");
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        e.add("salaries", "big");
        e.render();
        String errorExpecting = "1:23: anonymous template has 2 arg(s) but mapped across 3 value(s)" +
            NEWLINE +
            "context [anonymous] 1:23 passed 3 arg(s) to template /_sub1 with 2 declared arg(s)" +
            NEWLINE +
            "context [anonymous] 1:1 iterating through 3 values in zip map but template has 2 declared arguments" +
            NEWLINE;
        assertEquals(errorExpecting, errors.toString());
        assertRenderingResult("Ter@1, Tom@2", e);
    }

    @Test
    public void testStringTypeMismatch()
    {
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new LegacyBareStGroup();
        group.setListener(errors);
        ST e = new ST(group, "<trim(s)>");
        e.add("s", 34);
        e.render(); // generate the error
        String errorExpecting = "context [anonymous] 1:1 function trim expects a string not java.lang.Integer" +
            NEWLINE;
        assertEquals(errorExpecting, errors.toString());
    }

    @Test
    public void testStringTypeMismatch2()
    {
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = new LegacyBareStGroup();
        group.setListener(errors);
        ST e = new ST(group, "<strlen(s)>");
        e.add("s", 34);
        e.render(); // generate the error
        String errorExpecting = "context [anonymous] 1:1 function strlen expects a string not java.lang.Integer" +
            NEWLINE;
        assertEquals(errorExpecting, errors.toString());
    }
}
