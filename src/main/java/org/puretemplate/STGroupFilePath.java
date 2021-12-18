package org.puretemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import lombok.extern.slf4j.Slf4j;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.Token;
import org.puretemplate.error.ErrorType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;

/**
 * The internal representation of a single group file (which must end in ".stg"). Templates are only looked up in this
 * file or an import.
 */
@Slf4j
class STGroupFilePath extends GreenfieldStGroup
{
    /**
     * This method exists to ease the migration of test classes which previously constructed STGroupFile instances.
     */
    @VisibleForTesting
    static STGroupFilePath createWithDefaults(String filePath)
    {
        Path path = Paths.get(filePath);
        try (BufferedReader r = Files.newBufferedReader(path))
        {
            String sourceText = CharStreams.toString(r);
            return new STGroupFilePath(sourceText, path, StandardCharsets.UTF_8, '<', '>');
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private final String sourceText;
    private final Path file;

    protected boolean alreadyLoaded;

    public STGroupFilePath(String sourceText, Path file, Charset charset, char start, char stop)
    {
        super(charset, start, stop);
        this.sourceText = sourceText;
        this.file = file;
        String fileName = file.getFileName()
            .toString();

        if (!fileName.endsWith(GROUP_FILE_EXTENSION))
        {
            throw new IllegalArgumentException("Group file names must end in .stg: " + fileName);
        }
    }

    @Override
    public boolean isDictionary(String name)
    {
        load();
        return super.isDictionary(name);
    }

    @Override
    public boolean isDefined(String name)
    {
        load();
        return super.isDefined(name);
    }

    @Override
    public void importTemplates(Token fileNameToken)
    {
        log.debug("importTemplates({})", fileNameToken.getText());
        String importFileName = fileNameToken.getText();

        // do nothing upon syntax error
        if (importFileName == null || importFileName.equals("<missing STRING>"))
        {
            return;
        }
        importFileName = Misc.strip(importFileName, 1);

        STGroup g = loadImportableGroup(importFileName);
        if (g == null)
        {
            errMgr.compileTimeError(ErrorType.CANT_IMPORT, null, fileNameToken, importFileName);
        }
        else
        {
            g.setListener(getListener());
            importTemplates(g);
        }
    }

    private STGroup loadImportableGroup(String importFileName)
    {
        Path importPath = file.resolveSibling(importFileName);
        try
        {
            if (importPath.getFileName()
                .toString()
                .endsWith(TEMPLATE_FILE_EXTENSION))
            {
                return fetchTemplateFileAsGroup(importFileName, importPath);
            }
            else if (importPath.getFileName()
                .toString()
                .endsWith(GROUP_FILE_EXTENSION))
            {
                return fetchFileAsGroup(importPath);
            }
            else
            {
                return fetchDirAsGroup(importPath);
            }
        }
        catch (IOException e)
        {
            errMgr.internalError(null, "can't read from " + importFileName, e);
            return null;
        }
    }

    private STGroup fetchTemplateFileAsGroup(String importFileName, Path importPath) throws IOException
    {
        try (InputStream inputStream = Files.newInputStream(importPath))
        {
            ANTLRInputStream templateStream = new ANTLRInputStream(inputStream, charset.name());
            templateStream.name = importFileName;

            STGroup group = new LegacyBareStGroup(delimiterStartChar, delimiterStopChar);
            CompiledST code = group.loadTemplateFile("/", importFileName, templateStream);

            if (code != null)
            {
                return group;
            }
            else
            {
                return null;
            }
        }
    }

    private STGroup fetchFileAsGroup(Path importPath) throws IOException
    {
        try (BufferedReader reader = Files.newBufferedReader(importPath))
        {
            String importedSourceText = CharStreams.toString(reader);
            return new STGroupFilePath(importedSourceText, importPath, charset, delimiterStartChar, delimiterStopChar);
        }
    }

    private STGroup fetchDirAsGroup(Path importPath)
    {
        return new STGroupDirPath(importPath, charset, delimiterStartChar, delimiterStopChar);
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

        log.debug("loading group file {}", file);

        // no prefix since this group file is the entire group, nothing lives beneath it.
        loadGroup(file, sourceText, "/");

        log.debug("found {} templates in {} = {}", templates.size(), file, templates.keySet());
    }

    @Override
    public String show()
    {
        load();
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
        return file.getFileName()
            .toString();
    }
}
