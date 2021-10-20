package org.puretemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.Token;
import org.puretemplate.error.ErrorType;

/**
 * A directory or directory tree full of templates and/or group files. We load files on-demand. Dir search path: current
 * working dir then CLASSPATH (as a resource).  Do not look for templates outside of this dir subtree (except via
 * imports).
 */
@Slf4j
class STGroupDirPath extends GreenfieldStGroup
{
    private final String groupDirName;
    private final Path directory;

    public STGroupDirPath(Path directory)
    {
        this(directory, '<', '>');
    }

    public STGroupDirPath(Path directory, char delimiterStartChar, char delimiterStopChar)
    {
        this(directory, StandardCharsets.UTF_8, delimiterStartChar, delimiterStopChar);
    }

    public STGroupDirPath(Path directory, Charset charset, char delimiterStartChar, char delimiterStopChar)
    {
        super(charset, delimiterStartChar, delimiterStopChar);
        this.directory = directory;
        this.groupDirName = directory.getFileName()
            .toString();
    }

    @Override
    public void importTemplates(Token fileNameToken)
    {
        String msg = "import illegal in group files embedded in directory-based groups; " +
            "import " +
            fileNameToken.getText() +
            " in group " +
            this.getName();
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Load a template from directory or group file.  Group file is given precedence over directory with same name.
     * {@code name} is always fully-qualified.
     */
    @Override
    protected CompiledST load(String relativeName)
    {
        if (verbose)
        {
            System.out.println("STGroupPath.load(" + relativeName + ")");
        }

        if (!relativeName.startsWith("/"))
        {
            throw new IllegalArgumentException("name must start with a slash");
        }

        Path template = directory.resolve(relativeName.substring(1));

        // see if parent of template name is a group file
        Path groupFilePathWithoutExtension = template.getParent();
        Path groupFilePath = appendNameSuffix(groupFilePathWithoutExtension, GROUP_FILE_EXTENSION);
        try (InputStream inputStream = Files.newInputStream(groupFilePath))
        {
            // Load the templates from the given group file into this group
            String prefix = makeTemplatePrefix(groupFilePathWithoutExtension);
            loadGroupFile(groupFilePath, inputStream, prefix);
            return rawGetTemplate(relativeName);
        }
        catch (IOException e)
        {
            log.debug("Loading {} via group failed", groupFilePath, e);

            Path templateFile = appendNameSuffix(template, TEMPLATE_FILE_EXTENSION);

            // load t.st file
            try (InputStream templateInputStream = Files.newInputStream(templateFile))
            {
                return loadTemplateFile(templateFile, templateInputStream);
            }
            catch (IOException e2)
            {
                log.debug("Loading {} failed", templateFile, e2);
                return null;
            }
        }
    }

    private Path appendNameSuffix(Path directory, String suffix)
    {
        return directory.resolveSibling(directory.getFileName() + suffix);
    }

    /**
     * Load .st as relative file name relative to root by {@code prefix}.
     */
    public CompiledST loadTemplateFile(Path filePath, InputStream inputStream)
    {
        String unqualifiedName = Misc.getUnqualifiedName(filePath);
        if (verbose)
        {
            System.out.println("loadTemplateFile(" + unqualifiedName + ") in groupdir " + "from " + directory);
        }

        ANTLRInputStream fs;
        try
        {
            fs = new ANTLRInputStream(inputStream, charset.name());
            fs.name = unqualifiedName;
        }
        catch (IOException ioe)
        {
            if (verbose)
            {
                System.out.println(filePath + " doesn't exist");
            }
            errMgr.ioError(null, ErrorType.NO_SUCH_TEMPLATE, ioe, filePath.toString());
            return null;
        }

        String prefix = makeTemplatePrefix(filePath.getParent());
        return loadTemplateFile(prefix, unqualifiedName, fs);
    }

    /**
     * @return either {@code /} (if {@code path} points to the directory) or a path that starts and ends with a /
     */
    private String makeTemplatePrefix(Path path)
    {
        if (path.equals(directory))
        {
            return "/";
        }

        // Needs to start with & end in a slash, see Misc.getPrefix()
        StringBuilder result = new StringBuilder().append('/');

        Path relative = directory.relativize(path);
        for (Path name : relative)
        {
            result.append(name)
                .append('/');
        }

        return result.toString();
    }

    @Override
    public String getName()
    {
        return groupDirName;
    }

    @Override
    public String getFileName()
    {
        return directory.getFileName()
            .toString();
    }
}
