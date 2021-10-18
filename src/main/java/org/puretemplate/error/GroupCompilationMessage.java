package org.puretemplate.error;

import lombok.Getter;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;

@Getter
public final class GroupCompilationMessage extends Message
{
    /**
     * token inside group file
     */
    private final Token token;

    private final String srcName;

    public GroupCompilationMessage(ErrorType error, String srcName, Token t, Throwable cause, Object arg)
    {
        super(error, null, cause, arg);
        this.token = t;
        this.srcName = srcName;
    }

    @Override
    public String toString()
    {
        RecognitionException re = (RecognitionException) cause;
        int line = 0;
        int charPos = -1;
        if (token != null)
        {
            line = token.getLine();
            charPos = token.getCharPositionInLine();
        }
        else if (re != null)
        {
            line = re.line;
            charPos = re.charPositionInLine;
        }
        String filepos = line + ":" + charPos;
        if (srcName != null)
        {
            return srcName + " " + filepos + ": " + String.format(error.getMessage(), arg, arg2);
        }
        return filepos + ": " + String.format(error.getMessage(), arg, arg2);
    }
}
