package org.puretemplate.error;

import org.apiguardian.api.API;

/**
 * How to handle messages.
 */
@API(status = API.Status.MAINTAINED)
public interface ErrorListener
{
    void compileTimeError(Message msg);

    void runTimeError(Message msg);

    void ioError(Message msg);

    void internalError(Message msg);
}
