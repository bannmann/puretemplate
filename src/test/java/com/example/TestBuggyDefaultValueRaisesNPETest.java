package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.misc.ErrorBuffer;

class TestBuggyDefaultValueRaisesNPETest extends BaseTest
{
    /**
     * When the anonymous template specified as a default value for a formalArg contains a syntax error ST 4.0.2 emits a
     * NullPointerException error (after the syntax error)
     */
    @Test
    void testHandleBuggyDefaultArgument() throws IOException
    {
        String templates = "main(a={(<\"\")>}) ::= \"\"";
        ErrorBuffer errors = new ErrorBuffer();
        loadGroupViaDisk(templates, errors).getTemplate("main")
            .createContext()
            .render()
            .intoString();

        // Check the errors. This contained an "NullPointerException" before
        assertEquals("group.stg 1:12: mismatched input ')' expecting RDELIM" + NEWLINE, errors.toString());
    }
}
