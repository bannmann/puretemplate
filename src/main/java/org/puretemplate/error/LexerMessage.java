package org.puretemplate.error;

import lombok.Getter;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.apiguardian.api.API;
import org.puretemplate.Parsing;

/**
 * Used for errors encountered by the lexer.
 */
@API(status = API.Status.MAINTAINED)
@Getter
public final class LexerMessage extends Message
{
    private final String msg;

    /**
     * overall token pulled from group file
     */
    private final Token templateToken;

    private final String srcName;

    public LexerMessage(String srcName, String msg, Token templateToken, RecognitionException cause)
    {
        super(ErrorType.LEXER_ERROR, null, cause, null);
        this.msg = msg;
        this.templateToken = templateToken;
        this.srcName = srcName;
    }

    @Override
    public RecognitionException getCause()
    {
        return (RecognitionException) super.getCause();
    }

    @Override
    public String toString()
    {
        RecognitionException re = getCause();
        int line = re.line;
        int charPos = re.charPositionInLine;
        if (templateToken != null)
        {
            line += templateToken.getLine() - 1;
            charPos += templateToken.getCharPositionInLine() + Parsing.getTemplateDelimiterSize(templateToken);
        }
        String filepos = line + ":" + charPos;
        if (srcName != null)
        {
            return srcName + " " + filepos + ": " + String.format(error.getMessage(), msg);
        }
        return filepos + ": " + String.format(error.getMessage(), msg);
    }
}
