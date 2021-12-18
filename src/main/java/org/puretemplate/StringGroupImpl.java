package org.puretemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import lombok.NonNull;

import org.puretemplate.error.ErrorListener;

import com.github.mizool.core.validation.Nullable;
import com.google.common.io.CharStreams;

@Immutable
@ThreadSafe
final class StringGroupImpl extends AbstractGroup<STGroupString>
{
    private static STGroupString createStGroup(Source source, DelimiterConfig delimiterConfig)
    {
        try (Reader reader = source.open())
        {
            String sourceName = Misc.getUnqualifiedName(source.getName());
            String sourceText = CharStreams.toString(reader);
            return new STGroupString(sourceName, sourceText, delimiterConfig.getStart(), delimiterConfig.getStop());
        }
        catch (IOException e)
        {
            // TODO should we throw a regular IOException (for consistency with other I/O APIs)?
            throw new UncheckedIOException(e);
        }
    }

    public StringGroupImpl(
        @NonNull Source source,
        @NonNull DelimiterConfig delimiterConfig,
        boolean legacyRendering,
        @Nullable ErrorListener errorListener,
        @NonNull List<Handle> handles,
        @NonNull List<Group> imports)
    {
        super(createStGroup(source, delimiterConfig), legacyRendering, errorListener, handles, imports);
    }

    @Override
    public String getName()
    {
        return stGroup.getFileName();
    }
}
