package org.puretemplate.error;

import org.antlr.runtime.Token;
import org.apiguardian.api.API;

/**
 * Used for errors while compiling group files.
 */
@API(status = API.Status.MAINTAINED)
public interface GroupCompilationMessage extends Message
{
    String getSourceName();

    Token getToken();
}
