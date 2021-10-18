package org.puretemplate;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.puretemplate.error.ErrorListener;
import org.puretemplate.model.Aggregate;
import org.puretemplate.model.AttributeRenderer;
import org.puretemplate.model.ModelAdaptor;
import org.puretemplate.model.StringRenderer;

class GroupLoaderAction extends AbstractLoaderAction implements org.puretemplate.IGroupLoaderAction
{
    private Path directory;
    private final List<Group> imports = new ArrayList<>();
    private final List<Handle> handles = new ArrayList<>();
    private Charset charset = StandardCharsets.UTF_8;
    private boolean legacyRendering;
    private ErrorListener errorListener;

    @Override
    public void fromDirectory(String directoryPath)
    {
        fromDirectory(Path.of(directoryPath));
    }

    @Override
    public void fromDirectory(@NonNull File directory)
    {
        fromDirectory(directory.toPath());
    }

    @Override
    public void fromDirectory(@NonNull Path directory)
    {
        this.directory = directory;
    }

    @Override
    public void fromResourceDirectory(Class<?> reference, String relativePath)
    {
        fromDirectory(Resources.get(reference, relativePath));
    }

    @Override
    public void fromResourceDirectory(@NonNull String absolutePath)
    {
        fromDirectory(Resources.get(absolutePath));
    }

    @Override
    public void blank()
    {
        fromString("");
    }

    @Override
    public void importTemplates(@NonNull Group otherGroup)
    {
        imports.add(otherGroup);
    }

    /**
     * Register a renderer for all objects of a particular "kind" for all templates evaluated relative to this group.
     * Use {@code r} to render if object in question is an instance of {@code attributeType}.
     */
    @Override
    public <T> void registerAttributeRenderer(
        @NonNull Class<T> attributeType, @NonNull AttributeRenderer<? super T> renderer, @NonNull RendererDepth depth)
    {
        handles.add(new RendererHandle<>(attributeType, renderer, depth));
    }

    /**
     * Add an adaptor for a kind of object so PureTemplate knows how to pull properties from them. Add adaptors in
     * increasing order of specificity. Default adaptors are provided for {@link Object}, {@link Map}, templates and
     * {@link Aggregate}. Adaptors you add have priority over default adaptors.
     * <br>
     * If an adaptor for type {@code T} already exists, it is replaced by the {@code adaptor} argument.
     */
    @Override
    public <T> void registerModelAdaptor(@NonNull Class<T> attributeType, @NonNull ModelAdaptor<? super T> adaptor)
    {
        if (attributeType.isPrimitive())
        {
            throw new IllegalArgumentException("can't register ModelAdaptor for primitive type " +
                attributeType.getSimpleName());
        }

        handles.add(new AdaptorHandle<>(attributeType, adaptor));
    }

    @Override
    public void usingCharset(@NonNull Charset charset)
    {
        this.charset = charset;

        if (source instanceof InputStreamSource)
        {
            this.source = ((InputStreamSource) this.source).withCharset(charset);
        }
    }

    @Override
    public void withErrorListener(ErrorListener listener)
    {
        this.errorListener = listener;
    }

    /**
     * Changes the template behavior so that attribute renderers are also applied to text elements. <br>
     * <br>
     * The default behavior of PureTemplate is to apply registered renderers (e.g. {@link StringRenderer}) to attribute
     * expressions only. StringTemplate, however, always applied attribute renderers to text elements, as well. If you
     * need backwards compatibility, use this method to revert to the classic behavior.
     */
    @Override
    public void withLegacyRendering()
    {
        legacyRendering = true;
    }

    @Override
    public Group build()
    {
        if (directory != null)
        {
            return new DirectoryGroupImpl(directory,
                delimiterConfig,
                legacyRendering,
                errorListener,
                handles,
                charset,
                imports);
        }
        else if (file != null)
        {
            return new FileGroupImpl(source,
                file,
                charset,
                delimiterConfig,
                legacyRendering,
                errorListener,
                handles,
                imports);
        }
        else
        {
            return new StringGroupImpl(source, delimiterConfig, legacyRendering, errorListener, handles, imports);
        }
    }
}
