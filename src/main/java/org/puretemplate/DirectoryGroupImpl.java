package org.puretemplate;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import lombok.NonNull;

import org.puretemplate.error.ErrorListener;

@Immutable
@ThreadSafe
class DirectoryGroupImpl implements InternalGroup
{
    private final String name;
    private final STGroupDirPath stGroup;

    public DirectoryGroupImpl(
        @NonNull Path directory,
        @NonNull DelimiterConfig delimiterConfig,
        boolean legacyRendering,
        ErrorListener errorListener,
        @NonNull List<Handle> handles,
        @NonNull Charset charset,
        List<Group> imports)
    {
        name = Misc.getUnqualifiedName(directory);

        stGroup = new STGroupDirPath(directory, charset, delimiterConfig.getStart(), delimiterConfig.getStop());
        stGroup.setLegacyRendering(legacyRendering);

        if (errorListener != null)
        {
            stGroup.setListener(errorListener);
        }

        imports.stream()
            .map(InternalGroup.class::cast)
            .map(InternalGroup::getStGroup)
            .forEach(stGroup::importTemplates);

        for (Handle handle : handles)
        {
            handle.registerWith(stGroup);
        }
    }

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
