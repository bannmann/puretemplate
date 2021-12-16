package org.puretemplate.fs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.kohsuke.MetaInfServices;

import com.github.mizool.core.exception.NotYetImplementedException;

@API(status = API.Status.INTERNAL)
@MetaInfServices
public class ResourceFileSystemProvider extends FileSystemProvider
{
    private final ResourceFileSystem fileSystem = new ResourceFileSystem(this);

    @Override
    public String getScheme()
    {
        return ResourceFileSystem.URI_SCHEME;
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env)
    {
        throw new UnsupportedOperationException(
            "Resource file system instances cannot be created directly, use getPath() instead");
    }

    @Override
    public FileSystem getFileSystem(URI uri)
    {
        throw new UnsupportedOperationException(
            "Resource file system instances cannot be retrieved directly, use getPath() instead");
    }

    @Override
    public Path getPath(URI uri)
    {
        if (!uri.getScheme()
            .equals(ResourceFileSystem.URI_SCHEME))
        {
            throw new IllegalArgumentException("Invalid scheme " + uri.getScheme());
        }
        return fileSystem.getPath(uri.getSchemeSpecificPart());
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
    {
        // TODO decide whether this can/should be implemented
        throw new NotYetImplementedException();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options) throws IOException
    {
        // TODO verify options
        return ResourcePath.obtainFrom(path, ProviderMismatchException::new)
            .open();
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException
    {
        throw readOnlyException();
    }

    private IOException readOnlyException()
    {
        return new IOException("Resource file system is read-only");
    }

    @Override
    public void delete(Path path) throws IOException
    {
        throw readOnlyException();
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException
    {
        throw readOnlyException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException
    {
        throw readOnlyException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2)
    {
        return path instanceof ResourcePath && path2 instanceof ResourcePath && path.equals(path2);
    }

    @Override
    public boolean isHidden(Path path)
    {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path)
    {
        return fileSystem.getFileStoreSingleton();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes)
    {
        throw new NotYetImplementedException();
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
    {
        throw new NotYetImplementedException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
    {
        throw new NotYetImplementedException();
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
    {
        throw new NotYetImplementedException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException
    {
        throw readOnlyException();
    }
}
