package org.puretemplate;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.apiguardian.api.API;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.misc.InputSupplier;
import org.puretemplate.model.Aggregate;
import org.puretemplate.model.AttributeRenderer;
import org.puretemplate.model.ModelAdaptor;
import org.puretemplate.model.StringRenderer;

import com.github.mizool.core.validation.Nullable;

class GroupLoaderAction extends AbstractLoaderAction implements org.puretemplate.IGroupLoaderAction
{
    private Path directory;
    private final List<Group> imports = new ArrayList<>();
    private final List<Handle> handles = new ArrayList<>();
    private Charset charset = StandardCharsets.UTF_8;
    private boolean legacyRendering;
    private ErrorListener errorListener;

    @Override
    public void fromDirectory(@NonNull String directoryPath)
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
    public void fromResourceDirectory(@NonNull Class<?> reference, @NonNull String relativePath)
    {
        fromDirectory(Resources.get(reference, relativePath));
    }

    @Override
    public void fromResourceDirectory(@NonNull String absolutePath)
    {
        fromDirectory(Resources.get(absolutePath));
    }

    /**
     * Loads content from the file with the given path.
     *
     * @param filePath path to the file to read
     */
    @Override
    public void fromFile(@NonNull String filePath)
    {
        super.fromFile(filePath);
    }

    /**
     * Loads content from the given file.
     *
     * @param file the file to read
     */
    @Override
    public void fromFile(@NonNull File file)
    {
        super.fromFile(file);
    }

    /**
     * Loads content from the file referenced by the given path.
     *
     * @param file path to the file to read
     */
    @Override
    public void fromFile(@NonNull Path file)
    {
        super.fromFile(file);
    }

    /**
     * Loads content from the given input stream supplier. The stream is consumed entirely, but not closed.
     *
     * @param inputStreamSupplier a supplier for the input stream to use
     */
    @Override
    public void fromInputStream(@NonNull InputSupplier<InputStream> inputStreamSupplier)
    {
        super.fromInputStream(inputStreamSupplier);
    }

    /**
     * Loads content from the given input stream. The stream is consumed entirely, but not closed.
     *
     * @param inputStream the input stream to use
     */
    @Override
    public void fromInputStream(@NonNull InputStream inputStream)
    {
        super.fromInputStream(inputStream);
    }

    /**
     * Loads content from the given reader supplier. The reader is consumed entirely, but not closed.
     *
     * @param readerSupplier a supplier for the reader to use
     */
    @Override
    public void fromReader(@NonNull InputSupplier<Reader> readerSupplier)
    {
        super.fromReader(readerSupplier);
    }

    /**
     * Loads content from the given reader. The reader is consumed entirely, but not closed.
     *
     * @param reader the reader to use
     */
    @Override
    public void fromReader(@NonNull Reader reader)
    {
        super.fromReader(reader);
    }

    /**
     * Loads content from a resource file. <br>
     * <br>
     * <b>API status:</b> {@link API.Status#EXPERIMENTAL} because this method may yield unexpected results as it does
     * not yet use the class loader of the given class
     *
     * @param reference class from the package that the path is relative to
     * @param relativePath resource file path relative to the package of the given class
     */
    @API(status = API.Status.EXPERIMENTAL)
    @Override
    public void fromResourceFile(@NonNull Class<?> reference, @NonNull String relativePath)
    {
        super.fromResourceFile(reference, relativePath);
    }

    /**
     * Loads content from a resource file with the given absolute path. <br>
     * <br>
     * <b>API status:</b> {@link API.Status#EXPERIMENTAL} because this method does not yet use the {@code classLoader}
     * argument.
     *
     * @param classLoader <b>ignored</b>
     * @param absolutePath the absolute path of the resource to load
     *
     * @deprecated This method does not yet use the {@code classLoader} argument.
     */
    @API(status = API.Status.EXPERIMENTAL)
    @Deprecated
    @SuppressWarnings("java:S1133")
    @Override
    public void fromResourceFile(@NonNull ClassLoader classLoader, @NonNull String absolutePath)
    {
        super.fromResourceFile(classLoader, absolutePath);
    }

    /**
     * Loads content from a resource file with the given absolute path.
     *
     * @param absolutePath the absolute path of the resource to load
     */
    @Override
    public void fromResourceFile(@NonNull String absolutePath)
    {
        super.fromResourceFile(absolutePath);
    }

    /**
     * Loads content from the given string.
     *
     * @param source content to load
     */
    @Override
    public void fromString(@NonNull String source)
    {
        super.fromString(source);
    }

    /**
     * Sets the delimiters to use.  If this method is not called, PureTemplate defaults to '&lt;' and '&gt;'.
     *
     * @param start the start delimiter
     * @param stop the stop delimiter
     */
    @Override
    public void withDelimiters(char start, char stop)
    {
        super.withDelimiters(start, stop);
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
        @NonNull Class<T> attributeType,
        @NonNull AttributeRenderer<? super T> renderer,
        @NonNull Loader.RendererDepth depth)
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

    /**
     * Sets the error listener to use during loading and rendering.
     *
     * @param listener the listener to use, {@code null} to remove a previously set listener (if any)
     */
    @Override
    public void withErrorListener(@Nullable ErrorListener listener)
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
