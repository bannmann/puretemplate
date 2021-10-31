package org.puretemplate.error;

import lombok.Getter;

import org.antlr.runtime.Token;
import org.apiguardian.api.API;
import org.puretemplate.Parsing;

/**
 * Used for semantic errors that occur at compile time not during interpretation. For ST parsing ONLY not group
 * parsing.
 */
@API(status = API.Status.MAINTAINED)
@Getter
public final class TemplateCompilationMessage extends Message
{
    /**
     * overall token pulled from group file
     */
    private final Token templateToken;

    /**
     * token inside template
     */
    private final Token token;

    private final String srcName;

    public TemplateCompilationMessage(
        ErrorType error, String srcName, Token templateToken, Token t, Throwable cause, Object arg)
    {
        this(error, srcName, templateToken, t, cause, arg, null);
    }

    public TemplateCompilationMessage(
        ErrorType error, String srcName, Token templateToken, Token t, Throwable cause, Object arg, Object arg2)
    {
        super(error, null, cause, arg, arg2);
        this.templateToken = templateToken;
        this.token = t;
        this.srcName = srcName;
    }

    @Override
    public String toString()
    {
        int line = 0;
        int charPos = -1;
        if (token != null)
        {
            line = token.getLine();
            charPos = token.getCharPositionInLine();
            // check the input streams - if different then token is embedded in templateToken and we need to adjust the offset
            if (templateToken != null &&
                !templateToken.getInputStream()
                    .equals(token.getInputStream()))
            {
                line += templateToken.getLine() - 1;
                charPos += templateToken.getCharPositionInLine() + Parsing.getTemplateDelimiterSize(templateToken);
            }
        }
        String filepos = line + ":" + charPos;
        if (srcName != null)
        {
            return srcName + " " + filepos + ": " + String.format(error.getMessage(), arg, arg2);
        }
        return filepos + ": " + String.format(error.getMessage(), arg, arg2);
    }
}
