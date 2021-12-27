package org.puretemplate;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import lombok.NonNull;

import org.puretemplate.diagnostics.GroupDiagnostics;
import org.puretemplate.error.ErrorListener;

import com.github.mizool.core.validation.Nullable;

@Immutable
@ThreadSafe
abstract class AbstractGroup<T extends STGroup> implements Group
{
    protected final T stGroup;

    protected AbstractGroup(
        @NonNull T stGroup,
        boolean legacyRendering,
        @Nullable ErrorListener errorListener,
        @NonNull List<Handle> handles,
        @NonNull List<Group> imports)
    {
        this.stGroup = stGroup;
        this.stGroup.setLegacyRendering(legacyRendering);

        if (errorListener != null)
        {
            this.stGroup.setListener(errorListener);
        }

        this.stGroup.load();

        imports.stream()
            .map(AbstractGroup.class::cast)
            .map(abstractGroup -> abstractGroup.stGroup)
            .forEach(this.stGroup::importTemplates);

        for (Handle handle : handles)
        {
            handle.registerWith(this.stGroup);
        }
    }

    @Override
    public final Template getTemplate(@NonNull String name)
    {
        return new TemplateImpl(() -> stGroup.obtainInstanceOf(name));
    }

    @Override
    public GroupDiagnostics diagnostics()
    {
        return new GroupDiagnosticsImpl(stGroup);
    }
}
