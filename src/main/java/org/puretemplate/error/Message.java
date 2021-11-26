package org.puretemplate.error;

import org.apiguardian.api.API;
import org.puretemplate.misc.Location;

/**
 * Provides details about an error for use by an {@link ErrorListener}. This root class is used for IO and internal
 * errors.
 */
@API(status = API.Status.MAINTAINED)
public interface Message
{
    Object getArg();

    Object getArg2();

    Object getArg3();

    Throwable getCause();

    ErrorType getError();

    Location getLocation();
}
