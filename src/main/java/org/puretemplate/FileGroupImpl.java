package org.puretemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import lombok.NonNull;

import org.puretemplate.error.ErrorListener;

import com.github.mizool.core.validation.Nullable;
import com.google.common.io.CharStreams;

@Immutable
@ThreadSafe
final class FileGroupImpl extends AbstractGroup<STGroupFilePath>
{
    private static STGroupFilePath createStGroup(
        Source source, Path file, Charset charset, DelimiterConfig delimiterConfig)
    {
        try (Reader reader = source.open())
        {
            return new STGroupFilePath(CharStreams.toString(reader),
                file,
                charset,
                delimiterConfig.getStart(),
                delimiterConfig.getStop());
        }
        catch (IOException e)
        {
            // TODO should we throw a regular IOException (for consistency with other I/O APIs)?
            throw new UncheckedIOException(e);
        }
    }

    public FileGroupImpl(
        @NonNull Source source,
        @NonNull Path file,
        @NonNull Charset charset,
        @NonNull DelimiterConfig delimiterConfig,
        boolean legacyRendering,
        @Nullable ErrorListener errorListener,
        @NonNull List<Handle> handles,
        @NonNull List<Group> imports)
    {
        super(createStGroup(source, file, charset, delimiterConfig), legacyRendering, errorListener, handles, imports);
    }

    @Override
    public String getName()
    {
        return stGroup.getFileName();
    }
}
