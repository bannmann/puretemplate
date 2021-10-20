package org.puretemplate.fs;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.github.mizool.core.exception.CodeInconsistencyException;
import com.google.common.collect.Comparators;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

@RequiredArgsConstructor
@EqualsAndHashCode
class ResourcePath implements Path
{
    public static final Comparator<Iterable<Path>>
        COMPARATOR
        = Comparators.lexicographical(Comparator.<Path>naturalOrder());

    public static <E extends RuntimeException> ResourcePath obtainFrom(
        Path path, Function<String, E> exceptionConstructor)
    {
        return tryCast(path).orElseThrow(() -> newException(path, exceptionConstructor));
    }

    private static <E extends Exception> E newException(Path path, Function<String, E> constructor)
    {
        return constructor.apply("Path " + path + " is not a resource path");
    }

    private static Optional<ResourcePath> tryCast(Path path)
    {
        if (path instanceof ResourcePath)
        {
            return Optional.of((ResourcePath) path);
        }
        return Optional.empty();
    }

    @Getter
    @NonNull
    private final ResourceFileSystem fileSystem;

    private final ResourcePath root;

    @NonNull
    private final List<String> names;

    public ResourcePath(ResourceFileSystem fileSystem, String... names)
    {
        this(fileSystem, null, names);
    }

    public ResourcePath(ResourceFileSystem fileSystem, ResourcePath root, String... names)
    {
        this(fileSystem, root, List.of(names));
    }

    @Override
    public boolean isAbsolute()
    {
        return root != null;
    }

    private boolean isRelative()
    {
        return !isAbsolute();
    }

    @Override
    public Path getRoot()
    {
        return root;
    }

    @Override
    public Path getFileName()
    {
        if (names.isEmpty())
        {
            return null;
        }

        return new ResourcePath(fileSystem, names.get(names.size() - 1));
    }

    @Override
    public Path getParent()
    {
        // Paths representing the root directory have a non-null root and no names
        switch (names.size())
        {
            case 0:
                return null;
            case 1:
                return root;
            default:
                return new ResourcePath(fileSystem, root, names.subList(0, getNameCount() - 1));
        }
    }

    @Override
    public int getNameCount()
    {
        return names.size();
    }

    @Override
    public Path getName(int index)
    {
        if (names.isEmpty() || index > names.size())
        {
            throw new IllegalArgumentException();
        }
        return new ResourcePath(fileSystem, names.get(index));
    }

    public InputStream open() throws FileNotFoundException
    {
        // FIXME use specific class loader, not the one from ResourcePath
        InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream(getJoinedNames());
        if (inputStream == null)
        {
            throw new FileNotFoundException(toString());
        }
        return inputStream;
    }

    @Override
    public Path subpath(int beginIndex, int endIndex)
    {
        try
        {
            return new ResourcePath(fileSystem, null, names.subList(beginIndex, endIndex));
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean startsWith(@NonNull Path o)
    {
        ResourcePath other = obtainFrom(o, ProviderMismatchException::new);
        return other.fileSystem.equals(fileSystem) &&
            Objects.equals(root, other.root) &&
            NameLists.startsWith(names, other.names);
    }

    @Override
    public boolean endsWith(@NonNull Path o)
    {
        ResourcePath other = obtainFrom(o, ProviderMismatchException::new);
        if (other.isAbsolute())
        {
            return equals(other);
        }

        return NameLists.startsWith(Lists.reverse(names), Lists.reverse(other.names));
    }

    @Override
    public Path normalize()
    {
        return this;
    }

    @Override
    public Path resolve(@NonNull Path o)
    {
        ResourcePath other = obtainFrom(o, ProviderMismatchException::new);
        if (other.isAbsolute())
        {
            return other;
        }

        return new ResourcePath(fileSystem, root, NameLists.concat(names, other.names));
    }

    @Override
    public Path relativize(@NonNull Path o)
    {
        ResourcePath other = obtainFrom(o, IllegalArgumentException::new);
        if (isRelative() || other.isRelative() || !other.startsWith(this))
        {
            throw new IllegalArgumentException("Cannot relativize " + o + " based on " + this);
        }

        Iterator<String> myNames = names.iterator();
        Iterator<String> otherNames = other.names.iterator();
        while (myNames.hasNext() && otherNames.hasNext())
        {
            String mine = myNames.next();
            String theirs = otherNames.next();
            if (!mine.equals(theirs))
            {
                // Should not happen because we verified other.startsWith(this) earlier
                throw new CodeInconsistencyException();
            }
        }

        String[] remainingOtherNames = Iterators.toArray(otherNames, String.class);
        return new ResourcePath(fileSystem, remainingOtherNames);
    }

    @Override
    public URI toUri()
    {
        try
        {
            /*
             * We use a single slash instead of the // prefix known from http or even file because our URIs have no
             * 'authority' component in the sense of RFC 2396.
             */
            return new URI(ResourceFileSystem.URI_SCHEME + ":/" + toAbsolutePath());
        }
        catch (URISyntaxException e)
        {
            // Our goal is to ensure that this never happens
            throw new CodeInconsistencyException(e);
        }
    }

    @Override
    public ResourcePath toAbsolutePath()
    {
        if (isAbsolute())
        {
            return this;
        }
        return new ResourcePath(fileSystem, fileSystem.getRootDirectory(), names);
    }

    @Override
    public Path toRealPath(@NonNull LinkOption... options)
    {
        return toAbsolutePath();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path o)
    {
        // Note that unlike other methods, compareTo() is specified to throw a ClassCastException on provider mismatch
        ResourcePath other = (ResourcePath) o;
        return COMPARATOR.compare(this, other);
    }

    @Override
    public String toString()
    {
        String joinedNames = getJoinedNames();
        // Alleviate debugging by adding an indicator if this path is absolute
        if (isAbsolute())
        {
            return "/" + joinedNames;
        }
        return joinedNames;
    }

    private String getJoinedNames()
    {
        return String.join("/", names);
    }
}
