package org.puretemplate;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.exception.TemplateException;
import org.puretemplate.misc.ErrorBuffer;

class TestSyntaxErrors extends BaseTest
{
    @Test
    void testEmptyExpr()
    {
        String template = " <> ";
        STGroup group = new LegacyBareStGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try
        {
            group.defineTemplate("test", template);
        }
        catch (TemplateException se)
        {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:0: this doesn't look like a template: \" <> \"" + NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void testEmptyExpr2()
    {
        String template = "hi <> ";
        STGroup group = new LegacyBareStGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try
        {
            group.defineTemplate("test", template);
        }
        catch (TemplateException se)
        {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:3: doesn't look like an expression" + NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void testUnterminatedExpr()
    {
        String template = "hi <t()$";
        STGroup group = new LegacyBareStGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try
        {
            group.defineTemplate("test", template);
        }
        catch (TemplateException se)
        {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:7: invalid character '$'" +
            NEWLINE +
            "test 1:7: invalid character '<EOF>'" +
            NEWLINE +
            "test 1:7: premature EOF" +
            NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void testWeirdChar()
    {
        String template = "   <*>";
        STGroup group = new LegacyBareStGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try
        {
            group.defineTemplate("test", template);
        }
        catch (TemplateException se)
        {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:4: invalid character '*'" +
            NEWLINE +
            "test 1:0: this doesn't look like a template: \"   <*>\"" +
            NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void testWeirdChar2()
    {
        String template = "\n<\\\n";
        STGroup group = new LegacyBareStGroup();
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        try
        {
            group.defineTemplate("test", template);
        }
        catch (TemplateException se)
        {
            assert false;
        }
        String result = errors.toString();
        String expected = "test 1:2: invalid escaped char: '<EOF>'" +
            NEWLINE +
            "test 1:2: expecting '>', found '<EOF>'" +
            NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    void testValidButOutOfPlaceChar()
    {
        String templates = "foo() ::= <<hi <.> mom>>\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorListener errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("t.stg 1:15: doesn't look like an expression" + NEWLINE, errors.toString());
    }

    @Test
    void testValidButOutOfPlaceCharOnDifferentLine()
    {
        String templates = "foo() ::= \"hi <\n" + ".> mom\"\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("[t.stg 1:15: \\n in string, t.stg 1:14: doesn't look like an expression]",
            errors.getErrors()
                .toString());
    }

    @Test
    void testErrorInNestedTemplate()
    {
        String templates = "foo() ::= \"hi <name:{[<aaa.bb!>]}> mom\"\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorListener errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("t.stg 1:29: '!' came as a complete surprise to me" + NEWLINE, errors.toString());
    }

    @Test
    void testEOFInExpr()
    {
        String templates = "foo() ::= \"hi <name\"";
        writeFile(tmpdir, "t.stg", templates);

        ErrorListener errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("t.stg 1:19: premature EOF" + NEWLINE, errors.toString());
    }

    @Test
    void testEOFInExpr2()
    {
        String templates = "foo() ::= \"hi <name:{x|[<aaa.bb>]}\"\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorListener errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("t.stg 1:34: premature EOF" + NEWLINE, errors.toString());
    }

    @Test
    void testEOFInString()
    {
        String templates = "foo() ::= << <f(\"foo>>\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorListener errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("t.stg 1:20: EOF in string" + NEWLINE + "t.stg 1:20: premature EOF" + NEWLINE, errors.toString());
    }

    @Test
    void testNonterminatedComment()
    {
        String templates = "foo() ::= << <!foo> >>";
        writeFile(tmpdir, "t.stg", templates);

        ErrorListener errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("t.stg 1:20: Nonterminated comment starting at 1:1: '!>' missing" + NEWLINE, errors.toString());
    }

    @Test
    void testMissingRPAREN()
    {
        String templates = "foo() ::= \"hi <foo(>\"\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorListener errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("t.stg 1:19: '>' came as a complete surprise to me" + NEWLINE, errors.toString());
    }

    @Test
    void testRotPar()
    {
        String templates = "foo() ::= \"<a,b:t(),u()>\"\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorListener errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        group.load(); // force load
        assertEquals("t.stg 1:19: mismatched input ',' expecting RDELIM" + NEWLINE, errors.toString());
    }
}
