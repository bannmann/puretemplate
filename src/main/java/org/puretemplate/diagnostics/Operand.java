package org.puretemplate.diagnostics;

import javax.annotation.concurrent.Immutable;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
@Immutable
public interface Operand
{
    /**
     * @return the address ({@link OperandType#ADDR}), number ({@link OperandType#INT}) or pool index ({@link
     * OperandType#STRING}).
     */
    int getNumericValue();

    /**
     * @return {@code null} for {@link OperandType#ADDR} and {@link OperandType#INT} operands.
     */
    ConstantReference getStringConstant();

    OperandType getType();
}
