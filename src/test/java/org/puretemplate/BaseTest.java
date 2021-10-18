package org.puretemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.NonNull;

import org.junit.Before;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.misc.ErrorBuffer;

public abstract class BaseTest
{
    protected static final String NEWLINE = System.getProperty("line.separator");

    protected String tmpdir = null;

    protected Loader loader;

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

        assertTrue("expecting " + exceptionType.getSimpleName() + ", got " + getClassName(thrown),
            exceptionType.isInstance(thrown));
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

    @Before
    public void setUp()
    {
        loader = new Loader();
        STGroup.defaultGroup = new LegacyBareStGroup();
        Compiler.subtemplateCount = new AtomicInteger(0);

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
        STWriter stWriter = new AutoIndentWriter(stringWriter);
        stWriter.setLineWidth(STWriter.NO_WRAP);
        st.write(stWriter);
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

    protected ErrorBuffer getGroupLoadingErrors(String contents) throws IOException
    {
        ErrorBuffer errors = new ErrorBuffer();
        loadGroupViaDisk(contents, errors);
        return errors;
    }

    protected Group loadGroupFromString(String contents)
    {
        return loader.getGroup()
            .fromString(contents)
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
}
