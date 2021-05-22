package com.github.bannmann.puretemplate.misc;

import org.antlr.runtime.Token;
import com.github.bannmann.puretemplate.compiler.GroupParser;

/** Used for semantic errors that occur at compile time not during
 *  interpretation. For ST parsing ONLY not group parsing.
 */
public class STCompiletimeMessage extends STMessage {
    /** overall token pulled from group file */
    public Token templateToken;
    /** token inside template */
    public Token token;
    public String srcName;

    public STCompiletimeMessage(ErrorType error, String srcName, Token templateToken, Token t) {
        this(error, srcName, templateToken, t, null);
    }
    public STCompiletimeMessage(ErrorType error, String srcName, Token templateToken, Token t, Throwable cause) {
        this(error, srcName, templateToken, t, cause, null);
    }
    public STCompiletimeMessage(ErrorType error, String srcName, Token templateToken, Token t,
                                Throwable cause, Object arg)
    {
        this(error, srcName, templateToken, t, cause, arg, null);
    }
    public STCompiletimeMessage(ErrorType error, String srcName, Token templateToken,
                                Token t, Throwable cause, Object arg, Object arg2)
    {
        super(error, null, cause, arg, arg2);
        this.templateToken = templateToken;
        this.token = t;
        this.srcName = srcName;
    }

    @Override
    public String toString() {
        int line = 0;
        int charPos = -1;
        if ( token!=null ) {
            line = token.getLine();
            charPos = token.getCharPositionInLine();
            // check the input streams - if different then token is embedded in templateToken and we need to adjust the offset
            if ( templateToken!=null && !templateToken.getInputStream().equals(token.getInputStream()) ) {
                int templateDelimiterSize = 1;
                if ( templateToken.getType()== GroupParser.BIGSTRING || templateToken.getType()== GroupParser.BIGSTRING_NO_NL ) {
                    templateDelimiterSize = 2;
                }
                line += templateToken.getLine() - 1;
                charPos += templateToken.getCharPositionInLine() + templateDelimiterSize;
            }
        }
        String filepos = line+":"+charPos;
        if ( srcName!=null ) {
            return srcName+" "+filepos+": "+String.format(error.message, arg, arg2);
        }
        return filepos+": "+String.format(error.message, arg, arg2);
    }
}
