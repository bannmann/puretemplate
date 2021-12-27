package org.puretemplate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

import org.puretemplate.diagnostics.Statement;
import org.puretemplate.diagnostics.TemplateDiagnostics;

@RequiredArgsConstructor
final class TemplateDiagnosticsImpl implements TemplateDiagnostics
{
    private final CompiledST compiledST;

    @Override
    public void dump(Consumer<String> printer)
    {
        compiledST.dump(printer);
    }

    @Override
    public String getDump()
    {
        return compiledST.getDump();
    }

    @Override
    public List<Statement> getStatements()
    {
        return compiledST.getStatements();
    }

    @Override
    public String getStatementsAsString()
    {
        return compiledST.getStatementsAsString();
    }

    @Override
    public String getStrings()
    {
        return Arrays.toString(compiledST.strings);
    }
}
