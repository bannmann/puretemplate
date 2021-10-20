package org.puretemplate;

import java.nio.charset.Charset;

import lombok.NonNull;

import org.antlr.runtime.Token;

/**
 * @deprecated
 */
@Deprecated(forRemoval = true)
class LegacyBareStGroup extends STGroup
{
    public LegacyBareStGroup()
    {
    }

    public LegacyBareStGroup(char delimiterStartChar, char delimiterStopChar)
    {
        super(delimiterStartChar, delimiterStopChar);
    }

    public LegacyBareStGroup(@NonNull Charset charset, char delimiterStartChar, char delimiterStopChar)
    {
        super(charset, delimiterStartChar, delimiterStopChar);
    }

    public void importTemplates(Token fileNameToken)
    {
        /*
         * The original implementation of this method relied on STGroupDir, which was deleted. However, relevant tests
         * already migrated to the new path-based implementations, and this legacy class is not used by public APIs.
         */
        throw new UnsupportedOperationException();
    }
}
