package org.puretemplate.error;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.apiguardian.api.API;

/**
 * Used for errors encountered by the lexer.
 */
@API(status = API.Status.MAINTAINED)
public interface LexerMessage extends Message
{
    @Override
    RecognitionException getCause();

    String getMessage();

    String getSourceName();

    Token getTemplateToken();
}
