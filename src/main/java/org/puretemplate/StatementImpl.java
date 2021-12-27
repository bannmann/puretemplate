package org.puretemplate;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import org.puretemplate.diagnostics.Instruction;
import org.puretemplate.diagnostics.Operand;
import org.puretemplate.diagnostics.Statement;

import com.google.common.collect.ImmutableList;

@Value
@Builder
class StatementImpl implements Statement
{
    int address;
    @NonNull Instruction instruction;
    @NonNull ImmutableList<Operand> operands;
    int size;

    @Override
    public String toString()
    {
        return toString(Format.COMPACT);
    }
}
