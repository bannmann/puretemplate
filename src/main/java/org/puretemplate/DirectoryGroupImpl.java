package org.puretemplate;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import lombok.Getter;
import lombok.NonNull;

import org.puretemplate.error.ErrorListener;

import com.github.mizool.core.validation.Nullable;

@Immutable
@ThreadSafe
final class DirectoryGroupImpl extends AbstractGroup<STGroupDirPath>
{
    private static STGroupDirPath createStGroup(Path directory, Charset charset, DelimiterConfig delimiterConfig)
    {
        return new STGroupDirPath(directory, charset, delimiterConfig.getStart(), delimiterConfig.getStop());
    }

    @Getter
    private final String name;

    public DirectoryGroupImpl(
        @NonNull Path directory,
        @NonNull DelimiterConfig delimiterConfig,
        boolean legacyRendering,
        @Nullable ErrorListener errorListener,
        @NonNull List<Handle> handles,
        @NonNull Charset charset,
        @NonNull List<Group> imports)
    {
        super(createStGroup(directory, charset, delimiterConfig), legacyRendering, errorListener, handles, imports);
        name = Misc.getUnqualifiedName(directory);
    }
}
