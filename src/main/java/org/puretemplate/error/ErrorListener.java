package org.puretemplate.error;

/**
 * How to handle messages.
 */
public interface ErrorListener
{
    void compileTimeError(Message msg);

    void runTimeError(Message msg);

    void ioError(Message msg);

    void internalError(Message msg);
}
