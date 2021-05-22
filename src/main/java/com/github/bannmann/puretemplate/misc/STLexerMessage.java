package com.github.bannmann.puretemplate.misc;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;

import com.github.bannmann.puretemplate.compiler.GroupParser;

public class STLexerMessage extends STMessage
{
    public String msg;
    /**
     * overall token pulled from group file
     */
    public Token templateToken;
    public String srcName;

    public STLexerMessage(String srcName, String msg, Token templateToken, Throwable cause)
    {
        super(ErrorType.LEXER_ERROR, null, cause, null);
        this.msg = msg;
        this.templateToken = templateToken;
        this.srcName = srcName;
    }

    @Override
    public String toString()
    {
        RecognitionException re = (RecognitionException) cause;
        int line = re.line;
        int charPos = re.charPositionInLine;
        if (templateToken != null)
        {
            int templateDelimiterSize = 1;
            if (templateToken.getType() == GroupParser.BIGSTRING)
            {
                templateDelimiterSize = 2;
            }
            line += templateToken.getLine() - 1;
            charPos += templateToken.getCharPositionInLine() + templateDelimiterSize;
        }
        String filepos = line + ":" + charPos;
        if (srcName != null)
        {
            return srcName + " " + filepos + ": " + String.format(error.message, msg);
        }
        return filepos + ": " + String.format(error.message, msg);
    }
}
