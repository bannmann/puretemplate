package org.puretemplate;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

import lombok.NonNull;

import org.apiguardian.api.API;

class TemplateLoaderAction extends AbstractLoaderAction implements ITemplateLoaderAction
{
    private Group parentGroup;

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
    public void attachedToGroup(Group group)
    {
        parentGroup = group;
    }

    /**
     * Specify an alternate charset to use. If this method is not called, PureTemplate defaults to UTF-8.
     *
     * @param charset the charset to use
     */
    public void usingCharset(@NonNull Charset charset)
    {
        if (source instanceof InputStreamSource)
        {
            this.source = ((InputStreamSource) this.source).withCharset(charset);
        }
    }

    @Override
    public Template build()
    {
        // Read InputStreams/Readers now to ensure that TemplateImpl can invoke its supplier multiple times
        String sourceText = loadFrom(source);

        return new TemplateImpl(() -> createSt(sourceText));
    }

    private ST createSt(String sourceText)
    {
        // Parent group is mutually exclusive with specifying delimiters
        if (parentGroup != null)
        {
            InternalGroup parentGroupInternal = (InternalGroup) this.parentGroup;
            return new ST(parentGroupInternal.getStGroup(), sourceText);
        }
        else
        {
            return new ST(sourceText, delimiterConfig.getStart(), delimiterConfig.getStop());
        }
    }
}
