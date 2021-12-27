package org.puretemplate;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import org.puretemplate.diagnostics.ConstantReference;
import org.puretemplate.diagnostics.Operand;
import org.puretemplate.diagnostics.OperandType;

import com.github.mizool.core.validation.Nullable;

@Value
@Builder
class OperandImpl implements Operand
{
    @NonNull OperandType type;

    int numericValue;

    @Nullable
    ConstantReference stringConstant;
}
