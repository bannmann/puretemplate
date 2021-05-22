package com.github.bannmann.puretemplate.test;

import org.junit.Test;
import com.github.bannmann.puretemplate.STErrorListener;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.STGroupFile;
import com.github.bannmann.puretemplate.compiler.STException;
import com.github.bannmann.puretemplate.misc.ErrorBuffer;

import static org.junit.Assert.assertEquals;

public class TestSyntaxErrors extends BaseTest {
    @Test public void testEmptyExpr() throws Exception {
        String template = " <> ";
        STGroup group = new STGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try {
            group.defineTemplate("test", template);
        }
        catch (STException se) {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:0: this doesn't look like a template: \" <> \""+newline;
        assertEquals(expected, result);
    }

    @Test public void testEmptyExpr2() throws Exception {
        String template = "hi <> ";
        STGroup group = new STGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try {
            group.defineTemplate("test", template);
        }
        catch (STException se) {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:3: doesn't look like an expression"+newline;
        assertEquals(expected, result);
    }

    @Test public void testUnterminatedExpr() throws Exception {
        String template = "hi <t()$";
        STGroup group = new STGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try {
            group.defineTemplate("test", template);
        }
        catch (STException se) {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:7: invalid character '$'" +newline+
            "test 1:7: invalid character '<EOF>'" +newline+
            "test 1:7: premature EOF"+newline;
        assertEquals(expected, result);
    }

    @Test public void testWeirdChar() throws Exception {
        String template = "   <*>";
        STGroup group = new STGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try {
            group.defineTemplate("test", template);
        }
        catch (STException se) {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:4: invalid character '*'"+newline +
                          "test 1:0: this doesn't look like a template: \"   <*>\""+newline;
        assertEquals(expected, result);
    }

    @Test public void testWeirdChar2() throws Exception {
        String template = "\n<\\\n";
        STGroup group = new STGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try {
            group.defineTemplate("test", template);
        }
        catch (STException se) {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:2: invalid escaped char: '<EOF>'" + newline +
                          "test 1:2: expecting '>', found '<EOF>'"+newline;
        assertEquals(expected, result);
    }

    @Test public void testValidButOutOfPlaceChar() throws Exception {
        String templates =
            "foo() ::= <<hi <.> mom>>\n";
        writeFile(tmpdir, "t.stg", templates);

        STErrorListener errors = new ErrorBuffer();
        STGroupFile group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:15: doesn't look like an expression"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testValidButOutOfPlaceCharOnDifferentLine() throws Exception {
        String templates =
                "foo() ::= \"hi <\n" +
                ".> mom\"\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorBuffer errors = new ErrorBuffer();
        STGroupFile group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "[t.stg 1:15: \\n in string, t.stg 1:14: doesn't look like an expression]";
        String result = errors.errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testErrorInNestedTemplate() throws Exception {
        String templates =
            "foo() ::= \"hi <name:{[<aaa.bb!>]}> mom\"\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:29: '!' came as a complete surprise to me"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testEOFInExpr() throws Exception {
        String templates =
            "foo() ::= \"hi <name\"";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:19: premature EOF"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }


    @Test public void testEOFInExpr2() throws Exception {
        String templates =
            "foo() ::= \"hi <name:{x|[<aaa.bb>]}\"\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:34: premature EOF"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testEOFInString() throws Exception {
        String templates =
            "foo() ::= << <f(\"foo>>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:20: EOF in string"+newline +
                          "t.stg 1:20: premature EOF"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testNonterminatedComment() throws Exception {
        String templates =
            "foo() ::= << <!foo> >>";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected =
            "t.stg 1:20: Nonterminated comment starting at 1:1: '!>' missing" +newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testMissingRPAREN() throws Exception {
        String templates =
            "foo() ::= \"hi <foo(>\"\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:19: '>' came as a complete surprise to me"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test public void testRotPar() throws Exception {
        String templates =
            "foo() ::= \"<a,b:t(),u()>\"\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroupFile group;
        STErrorListener errors = new ErrorBuffer();
        group = new STGroupFile(tmpdir+"/"+"t.stg");
        group.setListener(errors);
        group.load(); // force load
        String expected = "t.stg 1:19: mismatched input ',' expecting RDELIM"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

}
