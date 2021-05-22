package com.github.bannmann.puretemplate.test;

import org.junit.Test;
import com.github.bannmann.puretemplate.STErrorListener;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.STGroupFile;
import com.github.bannmann.puretemplate.misc.ErrorBuffer;

import static org.junit.Assert.assertEquals;

public class TestGroupSyntaxErrors extends BaseTest {
    @Test public void testMissingImportString() throws Exception {
        String templates =
            "import\n" +
            "foo() ::= <<>>\n";
        writeFile(tmpdir, "t.stg", templates);

        STErrorListener errors = new ErrorBuffer();
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 2:0: mismatched input 'foo' expecting STRING"+newline+
            "t.stg 2:3: missing EOF at '('"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testImportNotString() throws Exception {
        String templates =
            "import Super.stg\n" +
            "foo() ::= <<>>\n";
        writeFile(tmpdir, "t.stg", templates);

        STErrorListener errors = new ErrorBuffer();
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:7: mismatched input 'Super' expecting STRING"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testMissingTemplate() throws Exception {
        String templates =
            "foo() ::= \n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 2:0: missing template at '<EOF>'"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testUnclosedTemplate() throws Exception {
        String templates =
            "foo() ::= {";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:11: missing final '}' in {...} anonymous template" +newline+
                          "t.stg 1:10: no viable alternative at input '{'"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testParen() throws Exception {
        String templates =
            "foo( ::= << >>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:5: no viable alternative at input '::='"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testNewlineInString() throws Exception {
        String templates =
            "foo() ::= \"\nfoo\"\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:11: \\n in string"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testParen2() throws Exception {
        String templates =
            "foo) ::= << >>\n" +
            "bar() ::= <<bar>>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:0: garbled template definition starting at 'foo'"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testArg() throws Exception {
        String templates =
            "foo(a,) ::= << >>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:6: missing ID at ')'"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testArg2() throws Exception {
        String templates =
            "foo(a,,) ::= << >>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected =
            "[t.stg 1:6: missing ID at ',', " +
            "t.stg 1:7: missing ID at ')', " +
            "t.stg 1:7: redefinition of parameter <missing ID>]";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testArg3() throws Exception {
        String templates =
            "foo(a b) ::= << >>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected =
            "[t.stg 1:6: no viable alternative at input 'b']";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testDefaultArgsOutOfOrder() throws Exception {
        String templates =
            "foo(a={hi}, b) ::= << >>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected =
            "[t.stg 1:12: required parameters (b) must appear before optional parameters]";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testArgumentRedefinition() throws Exception {
        String templates =
            "foo(a,b,a) ::= << >>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected =
            "[t.stg 1:8: redefinition of parameter a]";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testArgumentRedefinitionInSubtemplate() throws Exception {
        String templates =
            "foo(names) ::= <<" + newline +
            "<names, names, names:{a,b,a|}>" + newline +
            ">>" + newline;
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected =
            "[t.stg 1:43: redefinition of parameter a, " +
            "t.stg 1:38: anonymous template has 2 arg(s) but mapped across 3 value(s)]";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testErrorWithinTemplate() throws Exception {
        String templates =
            "foo(a) ::= \"<a b>\"\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "[t.stg 1:15: 'b' came as a complete surprise to me]";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testMap() throws Exception {
        String templates =
            "d ::= []\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "[t.stg 1:7: missing dictionary entry at ']']";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testMap2() throws Exception {
        String templates =
            "d ::= [\"k\":]\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "[t.stg 1:11: missing value for key at ']']";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testMap3() throws Exception {
        String templates =
            "d ::= [\"k\":{dfkj}}]\n"; // extra }
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "[t.stg 1:17: invalid character '}']";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testUnterminatedString() throws Exception {
        String templates =
            "f() ::= \""; // extra }
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        ErrorBuffer errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "[t.stg 1:9: unterminated string, t.stg 1:9: missing template at '<EOF>']";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }
}
