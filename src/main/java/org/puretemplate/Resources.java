package org.puretemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;

import lombok.experimental.UtilityClass;

/**
 * TODO document
 * <ul>
 *     <li>ptres file system works as if its working directory points to the root -> "com/foo.ext" and "/com/foo.ext" point to the same resource</li>
 *     <li>still, {@link #get(String)} requires passing "/com/foo.ext" to make it clear that {@code Resource.get("subdir")} would not load a resource relative to the calling class</li>
 * </ul>
 */
@UtilityClass
class Resources
{
    static
    {
        // Trigger auto discovery of file system providers
        FileSystemProvider.installedProviders();
    }

    public static Path get(String absolutePath)
    {
        if (!absolutePath.startsWith("/"))
        {
            throw new IllegalArgumentException("Path must be absolute");
        }

        ClassLoader classLoader = Thread.currentThread()
            .getContextClassLoader();
        if (classLoader == null)
        {
            classLoader = Resources.class.getClassLoader();
        }
        return get(classLoader, absolutePath);
    }

    public static Path get(ClassLoader classLoader, String absolutePath)
    {
        // FIXME don't ignore classLoader
        // TODO use one filesystem per classloader, identified by e.g. its index(!) in a set
        try
        {
            return Paths.get(new URI("ptres:" + absolutePath));
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("java:S1075")
    public static Path get(Class<?> reference, String relativePath)
    {
        if (relativePath.startsWith("/"))
        {
            throw new IllegalArgumentException("Path must be relative");
        }

        String referencePathString = "/" +
            reference.getName()
                .replace('.', '/');
        Path referencePath = get(reference.getClassLoader(), referencePathString);

        return referencePath.resolveSibling(relativePath);
    }
}
