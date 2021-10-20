package org.puretemplate.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.github.mizool.core.exception.NotYetImplementedException;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@RequiredArgsConstructor
class ResourceFileSystem extends FileSystem
{
    public static final String URI_SCHEME = "ptres";

    private final ResourceFileSystemProvider provider;

    @Override
    public ResourcePath getPath(@NonNull String first, @NonNull String... more)
    {
        String pathString = String.join("/", Lists.asList(first, more));
        List<String> names = Splitter.on('/')
            .omitEmptyStrings()
            .splitToList(pathString);

        ResourcePath root = null;
        if (pathString.startsWith("/"))
        {
            root = getRootDirectory();
        }

        return new ResourcePath(this, root, names);
    }

    @Override
    public FileSystemProvider provider()
    {
        return provider;
    }

    @Override
    public String getSeparator()
    {
        return "/";
    }

    @Override
    public boolean isReadOnly()
    {
        return true;
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }

    @Override
    public void close()
    {
        // Nothing to do
    }

    @Override
    public Iterable<Path> getRootDirectories()
    {
        return List.of(getRootDirectory());
    }

    ResourcePath getRootDirectory()
    {
        return getPath("");
    }

    @Override
    public Iterable<FileStore> getFileStores()
    {
        return List.of(getFileStoreSingleton());
    }

    public FileStore getFileStoreSingleton()
    {
        throw new NotYetImplementedException();
    }

    @Override
    public Set<String> supportedFileAttributeViews()
    {
        return Set.of("basic");
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService()
    {
        return new UserPrincipalLookupService()
        {
            @Override
            public UserPrincipal lookupPrincipalByName(String name) throws IOException
            {
                throw new UserPrincipalNotFoundException(name);
            }

            @Override
            public GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException
            {
                throw new UserPrincipalNotFoundException(group);
            }
        };
    }

    @Override
    public WatchService newWatchService()
    {
        throw new UnsupportedOperationException();
    }
}
