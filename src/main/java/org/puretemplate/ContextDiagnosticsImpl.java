package org.puretemplate;

import lombok.RequiredArgsConstructor;

import org.puretemplate.diagnostics.ContextDiagnostics;
import org.puretemplate.diagnostics.Event;
import org.puretemplate.diagnostics.EventListener;

@RequiredArgsConstructor
class ContextDiagnosticsImpl implements ContextDiagnostics
{
    private final ST st;

    public <E extends Event & Event.DistributionTarget> void addEventListener(
        EventListener listener, Class<E> eventInterface)
    {
        st.addEventListener(listener, eventInterface);
    }
}
