package org.puretemplate;

import lombok.experimental.UtilityClass;

import org.antlr.runtime.Token;

/**
 * Internal API class, do not use.
 */
@UtilityClass
public class Parsing
{
    public int getTemplateDelimiterSize(Token token)
    {
        switch (token.getType())
        {
            case GroupParser.BIGSTRING:
            case GroupParser.BIGSTRING_NO_NL:
                return 2;
            default:
                return 1;
        }
    }
}
