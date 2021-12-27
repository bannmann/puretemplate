package org.puretemplate.diagnostics;

import org.apiguardian.api.API;

/**
 * Provides low-level diagnostics access to a {@link org.puretemplate.Group}.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface GroupDiagnostics
{
    String getDump();
}
