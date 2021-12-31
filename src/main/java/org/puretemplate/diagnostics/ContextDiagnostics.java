package org.puretemplate.diagnostics;

import org.apiguardian.api.API;

/**
 * Provides low-level diagnostics access to a {@link org.puretemplate.Context}.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface ContextDiagnostics
{
    <E extends Event & Event.DistributionTarget> void addEventListener(EventListener listener, Class<E> eventInterface);
}
