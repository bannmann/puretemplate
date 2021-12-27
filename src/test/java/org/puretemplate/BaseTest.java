package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.NonNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;
import org.puretemplate.diagnostics.TemplateDiagnostics;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.misc.ErrorBuffer;
import org.slf4j.Logger;

public abstract class BaseTest
{
    protected static final String NEWLINE = System.getProperty("line.separator");

    protected String tmpdir = null;

    protected Loader loader;

    /**
     * Shorthand so we can write {@code args()} instead of {@code of()} or {@code Arguments.of()}.
     */
    protected static Arguments args(Object... arguments)
    {
        return Arguments.of(arguments);
    }

    protected <T extends Exception> void assertThrowsExceptionOfType(Runnable invocation, Class<T> exceptionType)
    {
        obtainThrownExceptionOfType(invocation, exceptionType);
    }

    protected <T extends Exception> T obtainThrownExceptionOfType(Runnable invocation, Class<T> exceptionType)
    {
        Exception thrown = null;
        try
        {
            invocation.run();
        }
        catch (Exception e)
        {
            thrown = e;
        }

        assertTrue(exceptionType.isInstance(thrown),
            "expecting " + exceptionType.getSimpleName() + ", got " + getClassName(thrown));
        return exceptionType.cast(thrown);
    }

    private String getClassName(Exception thrown)
    {
        return thrown == null
            ? null
            : thrown.getClass()
                .getSimpleName();
    }

    protected void assertThrowsIllegalArgumentException(Runnable invocation)
    {
        assertThrowsExceptionOfType(invocation, IllegalArgumentException.class);
    }

    @BeforeEach
    public void setUp()
    {
        loader = new Loader();
        STGroup.defaultGroup = new LegacyBareStGroup();

        String baseTestDirectory = System.getProperty("java.io.tmpdir");
        String testDirectory = getClass().getSimpleName() + "-" + System.currentTimeMillis();
        tmpdir = new File(baseTestDirectory, testDirectory).getAbsolutePath();
    }

    public static void writeFile(@NonNull String dir, @NonNull String fileName, @NonNull String content)
    {
        writeFile(Path.of(dir, fileName), content);
    }

    public static void writeFile(@NonNull Path dir, @NonNull String fileName, @NonNull String content)
    {
        writeFile(dir.resolve(fileName), content);
    }

    public static void writeFile(@NonNull Path file, @NonNull String content)
    {
        writeFile(file, content, StandardCharsets.UTF_8);
    }

    public static void writeFile(@NonNull Path file, @NonNull String content, @NonNull Charset charset)
    {
        try
        {
            Files.createDirectories(file.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(file, charset))
            {
                writer.write(content);
            }
        }
        catch (IOException ioe)
        {
            System.err.println("can't write file");
            ioe.printStackTrace(System.err);
        }
    }

    public static class User
    {
        public int id;
        public String name;

        public User(int id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public boolean isManager()
        {
            return true;
        }

        public boolean hasParkingSpot()
        {
            return true;
        }

        public String getName()
        {
            return name;
        }
    }

    public static class HashableUser extends User
    {
        public HashableUser(int id, String name)
        {
            super(id, name);
        }

        @Override
        public int hashCode()
        {
            return id;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof HashableUser)
            {
                HashableUser hu = (HashableUser) o;
                return this.id == hu.id && this.name.equals(hu.name);
            }
            return false;
        }
    }

    public String getRandomDir() throws IOException
    {
        return getRandomDirPath().toString();
    }

    public Path getRandomDirPath() throws IOException
    {
        Path result = Path.of(tmpdir, "dir" + (int) (Math.random() * 100000));
        Files.createDirectories(result);
        return result.toAbsolutePath();
    }

    protected void assertRenderingResult(String expecting, Context context)
    {
        assertEquals(expecting,
            context.render()
                .intoString());
    }

    protected void assertRenderingResult(String expecting, ST st) throws IOException
    {
        StringWriter stringWriter = new StringWriter();
        TemplateWriter templateWriter = new AutoIndentWriter(stringWriter);
        templateWriter.setLineWidth(TemplateWriter.NO_WRAP);
        st.write(templateWriter);
        String result = stringWriter.toString();
        assertEquals(expecting, result);
    }

    protected void assertNoArgRenderingResult(String expecting, Group group, String templateName)
    {
        assertRenderingResult(expecting,
            group.getTemplate(templateName)
                .createContext());
    }

    protected void assertNoArgRenderingResult(String expecting, String template)
    {
        Context context = loader.getTemplate()
            .fromString(template)
            .build()
            .createContext();
        assertRenderingResult(expecting, context);
    }

    protected Context makeTemplateContext(String s)
    {
        return loader.getTemplate()
            .fromString(s)
            .build()
            .createContext();
    }

    protected Template loadTemplateFromStringUsingBlankGroup(String template, ErrorListener errors)
    {
        // At some point, loader.getTemplate() will support withErrorListener() as well. Until then, use a blank group.
        Group group = loader.getGroup()
            .blank()
            .withErrorListener(errors)
            .build();

        return loader.getTemplate()
            .fromString(template)
            .attachedToGroup(group)
            .build();
    }

    protected ErrorBuffer getGroupLoadingErrors(String contents) throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();
        loadGroupViaDisk(contents, errors);
        return errors;
    }

    protected Group loadGroupFromString(String contents)
    {
        return loadGroupFromString(contents, null);
    }

    protected Group loadGroupFromString(String contents, ErrorListener errorListener)
    {
        return loader.getGroup()
            .fromString(contents)
            .withErrorListener(errorListener)
            .build();
    }

    protected Group loadGroupViaDisk(String contents) throws IOException
    {
        return loadGroupViaDisk(contents, null);
    }

    protected Group loadGroupViaDisk(String contents, ErrorListener errorListener) throws IOException
    {
        Path file = getRandomDirPath().resolve("group.stg");
        writeFile(file, contents);

        return loader.getGroup()
            .fromFile(file)
            .withErrorListener(errorListener)
            .build();
    }

    protected String renderGroupTemplate(Group group, String templateName)
    {
        return group.getTemplate(templateName)
            .createContext()
            .render()
            .intoString();
    }

    protected void dump(Logger logger, Template template)
    {
        dump(logger, template.diagnostics());
    }

    protected void dump(Logger logger, TemplateDiagnostics templateDiagnostics)
    {
        dump(logger, templateDiagnostics.getDump());
    }

    protected void dump(Logger logger, CompiledST compiledST)
    {
        dump(logger, compiledST.getDump());
    }

    private void dump(Logger logger, String dump)
    {
        logger.info("Dump:\n{}", dump);
    }
}
