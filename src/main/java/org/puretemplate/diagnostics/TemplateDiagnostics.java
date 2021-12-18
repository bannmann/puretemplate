package org.puretemplate.diagnostics;

import java.util.function.Consumer;

import org.apiguardian.api.API;

/**
 * Provides low-level diagnostics access to the static contents of a {@link org.puretemplate.Template}.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface TemplateDiagnostics
{
    void dump(Consumer<String> printer);

    String getDump();

    String getInstructions();

    String getStrings();
}
