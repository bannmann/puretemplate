package org.puretemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import lombok.NonNull;

import org.puretemplate.error.ErrorListener;

import com.google.common.io.CharStreams;

@Immutable
@ThreadSafe
class StringGroupImpl implements InternalGroup
{
    private final String name;
    private final STGroup stGroup;

    public StringGroupImpl(
        @NonNull Source source,
        @NonNull DelimiterConfig delimiterConfig,
        boolean legacyRendering,
        ErrorListener errorListener,
        @NonNull List<Handle> handles,
        List<Group> imports)
    {
        try (Reader reader = source.open())
        {
            name = Misc.getUnqualifiedName(source.getName());
            String sourceText = CharStreams.toString(reader);

            stGroup = new STGroupString(name, sourceText, delimiterConfig.getStart(), delimiterConfig.getStop());
            stGroup.setLegacyRendering(legacyRendering);

            if (errorListener != null)
            {
                stGroup.setListener(errorListener);
            }

            for (Handle handle : handles)
            {
                handle.registerWith(stGroup);
            }

            stGroup.load();

            imports.stream()
                .map(InternalGroup.class::cast)
                .map(InternalGroup::getStGroup)
                .forEach(stGroup::importTemplates);
        }
        catch (IOException e)
        {
            // TODO should we throw a regular IOException (for consistency with other I/O APIs)?
            throw new UncheckedIOException(e);
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
