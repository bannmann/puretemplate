package org.puretemplate;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Immutable
@ThreadSafe
@RequiredArgsConstructor
class WrappingGroupImpl implements InternalGroup
{
    private final String name;
    private final STGroup stGroup;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public STGroup getStGroup()
    {
        return stGroup;
    }

    @Override
    public Template getTemplate(@NonNull String name)
    {
        return new TemplateImpl(() -> stGroup.obtainInstanceOf(name));
    }
}
