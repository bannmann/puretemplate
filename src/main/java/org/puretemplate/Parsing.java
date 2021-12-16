package org.puretemplate;

import lombok.experimental.UtilityClass;

import org.antlr.runtime.Token;

@UtilityClass
class Parsing
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
