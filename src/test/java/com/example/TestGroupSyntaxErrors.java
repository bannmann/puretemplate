package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.misc.ErrorBuffer;

class TestGroupSyntaxErrors extends BaseTest
{
    @Test
    void testMissingImportString() throws IOException
    {
        String templates = "import\n" + "foo() ::= <<>>\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        String expected = "group.stg 2:0: mismatched input 'foo' expecting STRING" +
            NEWLINE +
            "group.stg 2:3: missing EOF at '('" +
            NEWLINE;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    void testImportNotString() throws IOException
    {
        String templates = "import Super.stg\n" + "foo() ::= <<>>\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("group.stg 1:7: mismatched input 'Super' expecting STRING" + NEWLINE, errors.toString());
    }

    @Test
    void testMissingTemplate() throws IOException
    {
        String templates = "foo() ::= \n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("group.stg 2:0: missing template at '<EOF>'" + NEWLINE, errors.toString());
    }

    @Test
    void testUnclosedTemplate() throws IOException
    {
        String templates = "foo() ::= {";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        String expected = "group.stg 1:11: missing final '}' in {...} anonymous template" +
            NEWLINE +
            "group.stg 1:10: no viable alternative at input '{'" +
            NEWLINE;
        String result = errors.toString();
        assertEquals(expected, result);
    }

    @Test
    void testParen() throws IOException
    {
        String templates = "foo( ::= << >>\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("group.stg 1:5: no viable alternative at input '::='" + NEWLINE, errors.toString());
    }

    @Test
    void testNewlineInString() throws IOException
    {
        String templates = "foo() ::= \"\nfoo\"\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("group.stg 1:11: \\n in string" + NEWLINE, errors.toString());
    }

    @Test
    void testParen2() throws IOException
    {
        String templates = "foo) ::= << >>\n" + "bar() ::= <<bar>>\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("group.stg 1:0: garbled template definition starting at 'foo'" + NEWLINE, errors.toString());
    }

    @Test
    void testArg() throws IOException
    {
        String templates = "foo(a,) ::= << >>\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("group.stg 1:6: missing ID at ')'" + NEWLINE, errors.toString());
    }

    @Test
    void testArg2() throws IOException
    {
        String templates = "foo(a,,) ::= << >>\n";
        writeFile(tmpdir, "t.stg", templates);

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        String expected = "[group.stg 1:6: missing ID at ',', " +
            "group.stg 1:7: missing ID at ')', " +
            "group.stg 1:7: redefinition of parameter <missing ID>]";
        String result = errors.getErrors()
            .toString();
        assertEquals(expected, result);
    }

    @Test
    void testArg3() throws IOException
    {
        String templates = "foo(a b) ::= << >>\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("[group.stg 1:6: no viable alternative at input 'b']",
            errors.getErrors()
                .toString());
    }

    @Test
    void testDefaultArgsOutOfOrder() throws IOException
    {
        String templates = "foo(a={hi}, b) ::= << >>\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("[group.stg 1:12: required parameters (b) must appear before optional parameters]",
            errors.getErrors()
                .toString());
    }

    @Test
    void testArgumentRedefinition() throws IOException
    {
        String templates = "foo(a,b,a) ::= << >>\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("[group.stg 1:8: redefinition of parameter a]",
            errors.getErrors()
                .toString());
    }

    @Test
    void testArgumentRedefinitionInSubtemplate() throws IOException
    {
        String templates = "foo(names) ::= <<" + NEWLINE + "<names, names, names:{a,b,a|}>" + NEWLINE + ">>" + NEWLINE;

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        String expected = "[group.stg 1:43: redefinition of parameter a, " +
            "group.stg 1:38: anonymous template has 2 arg(s) but mapped across 3 value(s)]";
        String result = errors.getErrors()
            .toString();
        assertEquals(expected, result);
    }

    @Test
    void testErrorWithinTemplate() throws IOException
    {
        String templates = "foo(a) ::= \"<a b>\"\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("[group.stg 1:15: 'b' came as a complete surprise to me]",
            errors.getErrors()
                .toString());
    }

    @Test
    void testMap() throws IOException
    {
        String templates = "d ::= []\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("[group.stg 1:7: missing dictionary entry at ']']",
            errors.getErrors()
                .toString());
    }

    @Test
    void testMap2() throws IOException
    {
        String templates = "d ::= [\"k\":]\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("[group.stg 1:11: missing value for key at ']']",
            errors.getErrors()
                .toString());
    }

    @Test
    void testMap3() throws IOException
    {
        String templates = "d ::= [\"k\":{dfkj}}]\n"; // extra }

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("[group.stg 1:17: invalid character '}']",
            errors.getErrors()
                .toString());
    }

    @Test
    void testUnterminatedString() throws IOException
    {
        String templates = "f() ::= \""; // extra }

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("[group.stg 1:9: unterminated string, group.stg 1:9: missing template at '<EOF>']",
            errors.getErrors()
                .toString());
    }
}
