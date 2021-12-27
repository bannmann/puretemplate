package org.puretemplate;

import lombok.Builder;
import lombok.Value;

import org.puretemplate.diagnostics.ConstantReference;

import com.github.mizool.core.validation.Nullable;

@Value
@Builder
class ConstantReferenceImpl implements ConstantReference
{
    boolean valid;

    @Nullable
    String value;
}
