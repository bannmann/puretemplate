package org.puretemplate.diagnostics;

import javax.annotation.concurrent.Immutable;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
@Immutable
public interface ConstantReference
{
    /**
     * @return {@code true} if the constant pool index is valid, {@code false} otherwise.
     */
    boolean isValid();

    /**
     * @return the constant string, may be {@code null}. May also be {@code null} if the pool index was invalid.
     */
    String getValue();
}
