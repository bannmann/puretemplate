package com.github.bannmann.puretemplate.misc;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;

/** */
public class STGroupCompiletimeMessage extends STMessage {
    /** token inside group file */
    public Token token;
    public String srcName;

    public STGroupCompiletimeMessage(ErrorType error, String srcName, Token t, Throwable cause) {
        this(error, srcName, t, cause, null);
    }
    public STGroupCompiletimeMessage(ErrorType error, String srcName, Token t,
                                     Throwable cause, Object arg)
    {
        this(error, srcName, t, cause, arg, null);
    }
    public STGroupCompiletimeMessage(ErrorType error, String srcName,
                                     Token t, Throwable cause, Object arg, Object arg2)
    {
        super(error, null, cause, arg, arg2);
        this.token = t;
        this.srcName = srcName;
    }

    @Override
    public String toString() {
        RecognitionException re = (RecognitionException)cause;
        int line = 0;
        int charPos = -1;
        if ( token!=null ) {
            line = token.getLine();
            charPos = token.getCharPositionInLine();
        }
        else if ( re!=null ) {
            line = re.line;
            charPos = re.charPositionInLine;
        }
        String filepos = line+":"+charPos;
        if ( srcName!=null ) {
            return srcName+" "+filepos+": "+String.format(error.message, arg, arg2);
        }
        return filepos+": "+String.format(error.message, arg, arg2);
    }
}
