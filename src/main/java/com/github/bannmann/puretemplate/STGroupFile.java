package com.github.bannmann.puretemplate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.github.bannmann.puretemplate.compiler.CompiledST;
import com.github.bannmann.puretemplate.compiler.STException;
import com.github.bannmann.puretemplate.misc.ErrorType;
import com.github.bannmann.puretemplate.misc.Misc;

/**
 * The internal representation of a single group file (which must end in ".stg").  If we fail to find a group file, look
 * for it via the CLASSPATH as a resource.  Templates are only looked up in this file or an import.
 */
public class STGroupFile extends STGroup
{
    /**
     * Just records how user "spelled" the file name they wanted to load. The url is the key field here for loading
     * content.
     *
     * If they use ctor with URL arg, this field is null.
     */
    public String fileName;

    /**
     * Where to find the group file. NonNull.
     */
    public URL url;

    protected boolean alreadyLoaded = false;

    /**
     * Load a file relative to current directory or from root or via CLASSPATH.
     */
    public STGroupFile(String fileName)
    {
        this(fileName, '<', '>');
    }

    public STGroupFile(String fileName, char delimiterStartChar, char delimiterStopChar)
    {
        super(delimiterStartChar, delimiterStopChar);
        if (!fileName.endsWith(GROUP_FILE_EXTENSION))
        {
            throw new IllegalArgumentException("Group file names must end in .stg: " + fileName);
        }
        //try {
        File f = new File(fileName);
        if (f.exists())
        {
            try
            {
                url = f.toURI()
                    .toURL();
            }
            catch (MalformedURLException e)
            {
                throw new STException("can't load group file " + fileName, e);
            }
            if (verbose)
            {
                System.out.println("STGroupFile(" + fileName + ") == file " + f.getAbsolutePath());
            }
        }
        else
        {
            // try in classpath
            url = getURL(fileName);
            if (url == null)
            {
                throw new IllegalArgumentException("No such group file: " + fileName);
            }
            if (verbose)
            {
                System.out.println("STGroupFile(" + fileName + ") == url " + url);
            }
        }
        this.fileName = fileName;
    }

    public STGroupFile(String fullyQualifiedFileName, String encoding)
    {
        this(fullyQualifiedFileName, encoding, '<', '>');
    }

    public STGroupFile(String fullyQualifiedFileName, String encoding, char delimiterStartChar, char delimiterStopChar)
    {
        this(fullyQualifiedFileName, delimiterStartChar, delimiterStopChar);
        this.encoding = encoding;
    }

    /**
     * Pass in a URL with the location of a group file. E.g., STGroup g = new STGroupFile(loader.getResource("org/foo/templates/g.stg"),
     * "UTF-8", '<', '>');
     */
    public STGroupFile(URL url, String encoding, char delimiterStartChar, char delimiterStopChar)
    {
        super(delimiterStartChar, delimiterStopChar);
        if (url == null)
        {
            throw new IllegalArgumentException("URL to group file cannot be null");
        }
        this.url = url;
        this.encoding = encoding;
        this.fileName = null;
    }

    /**
     * Convenience ctor
     */
    public STGroupFile(URL url)
    {
        this(url, "UTF-8", '<', '>');
    }

    @Override
    public boolean isDictionary(String name)
    {
        if (!alreadyLoaded)
        {
            load();
        }
        return super.isDictionary(name);
    }

    @Override
    public boolean isDefined(String name)
    {
        if (!alreadyLoaded)
        {
            load();
        }
        return super.isDefined(name);
    }

    @Override
    public synchronized void unload()
    {
        super.unload();
        alreadyLoaded = false;
    }

    @Override
    protected synchronized CompiledST load(String name)
    {
        if (!alreadyLoaded)
        {
            load();
        }
        return rawGetTemplate(name);
    }

    @Override
    public synchronized void load()
    {
        if (alreadyLoaded)
        {
            return;
        }
        alreadyLoaded = true; // do before actual load to say we're doing it
        // no prefix since this group file is the entire group, nothing lives
        // beneath it.
        if (verbose)
        {
            System.out.println("loading group file " + url.toString());
        }
        loadGroupFile("/", url.toString());
        if (verbose)
        {
            System.out.println("found " +
                templates.size() +
                " templates in " +
                url.toString() +
                " = " +
                templates.keySet());
        }
    }

    @Override
    public String show()
    {
        if (!alreadyLoaded)
        {
            load();
        }
        return super.show();
    }

    @Override
    public String getName()
    {
        return Misc.getFileNameNoSuffix(getFileName());
    }

    @Override
    public String getFileName()
    {
        if (fileName != null)
        {
            return fileName;
        }
        return url.getFile();
    }

    @Override
    public URL getRootDirURL()
    {
        //      System.out.println("url of "+fileName+" is "+url.toString());
        String parent = Misc.stripLastPathElement(url.toString());
        if (parent.endsWith(".jar!"))
        {
            parent = parent + "/."; // whooops. at the root so add "current dir" after jar spec
        }
        try
        {
            URL parentURL = new URL(parent);
            //          System.out.println("parent URL "+parentURL.toString());
            return parentURL;
        }
        catch (MalformedURLException mue)
        {
            errMgr.runTimeError(null, null, ErrorType.INVALID_TEMPLATE_NAME, mue, parent);
        }
        return null;
    }
}
