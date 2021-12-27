package org.puretemplate;

import lombok.RequiredArgsConstructor;

import org.puretemplate.diagnostics.GroupDiagnostics;

@RequiredArgsConstructor
final class GroupDiagnosticsImpl implements GroupDiagnostics
{
    private final STGroup stGroup;

    @Override
    public String getDump()
    {
        return stGroup.getDump();
    }
}
