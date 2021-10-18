package org.puretemplate;

import static org.junit.Assert.assertEquals;

import lombok.experimental.UtilityClass;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;

@UtilityClass
public class Tokens
{
    public static void checkTokens(String template, String expected)
    {
        checkTokens(template, expected, '<', '>');
    }

    public static void checkTokens(String template, String expected, char delimiterStartChar, char delimiterStopChar)
    {
        STLexer lexer = new STLexer(STGroup.DEFAULT_ERR_MGR,
            new ANTLRStringStream(template),
            null,
            delimiterStartChar,
            delimiterStopChar);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        int i = 1;
        Token t = tokens.LT(i);
        while (t.getType() != Token.EOF)
        {
            if (i > 1)
            {
                buf.append(", ");
            }
            buf.append(t);
            i++;
            t = tokens.LT(i);
        }
        buf.append("]");
        String result = buf.toString();
        assertEquals(expected, result);
    }
}
