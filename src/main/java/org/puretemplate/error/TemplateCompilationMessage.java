package org.puretemplate.error;

import org.antlr.runtime.Token;
import org.apiguardian.api.API;

/**
 * Used for errors while compiling template files.
 */
@API(status = API.Status.MAINTAINED)
public interface TemplateCompilationMessage extends Message
{
    String getSourceName();

    Token getTemplateToken();

    Token getToken();
}
