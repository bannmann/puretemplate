package org.puretemplate.diagnostics;

import java.util.List;
import java.util.function.Consumer;

import org.apiguardian.api.API;

/**
 * Provides low-level diagnostics access to a {@link org.puretemplate.Template}.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface TemplateDiagnostics
{
    void dump(Consumer<String> printer);

    String getDump();

    List<Statement> getStatements();

    String getStatementsAsString();

    String getStrings();
}
