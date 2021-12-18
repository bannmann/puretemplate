package org.puretemplate;

import java.util.Arrays;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

import org.puretemplate.diagnostics.TemplateDiagnostics;

@RequiredArgsConstructor
class TemplateDiagnosticsImpl implements TemplateDiagnostics
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
    public String getInstructions()
    {
        return compiledST.instrs();
    }

    @Override
    public String getStrings()
    {
        return Arrays.toString(compiledST.strings);
    }
}
