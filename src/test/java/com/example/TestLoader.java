package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.Template;

class TestLoader extends BaseTest
{
    public static final String GREETING_TEMPLATE = "hi <name>!";

    @Test
    void testFromString()
    {
        Template template = loader.getTemplate()
            .fromString(GREETING_TEMPLATE)
            .build();
        assertTemplateWorks(template);
    }

    @Test
    void testFromStringWithDelimiters()
    {
        Template template = loader.getTemplate()
            .fromString("hi $name$!")
            .withDelimiters('$', '$')
            .build();
        assertTemplateWorks(template);
    }

    private void assertTemplateWorks(Template template)
    {
        Context context = template.createContext()
            .add("name", "Ter");

        assertRenderingResult("hi Ter!", context);
    }

    @Test
    void testFromInputStream()
    {
        byte[] bytes = GREETING_TEMPLATE.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        Template template = loader.getTemplate()
            .fromInputStream(stream)
            .build();

        assertTemplateWorks(template);
    }

    @Test
    void testGroupFromString()
    {
        Group group = loadGroupFromString("a(x) ::= <<foo>>\n" + "b() ::= <<bar>>\n");

        assertEquals("foo", renderGroupTemplate(group, "a"));
        assertEquals("bar", renderGroupTemplate(group, "b"));
    }

    @Test
    void testGroupFromResourceFile()
    {
        Group group = loader.getGroup()
            .fromResourceFile(getClass(), "simple.stg")
            .build();

        assertEquals("simple.stg", group.getName());

        String result = renderGroupTemplate(group, "a");

        assertEquals("foo", result);
    }

    @Test
    void testGroupFromResourceDirectory()
    {
        Group group = loader.getGroup()
            .fromResourceDirectory(getClass(), "groupdir")
            .build();

        assertEquals("groupdir", group.getName());

        assertNoArgRenderingResult("foo", group, "foo");
    }

    @Test
    void testGroupFromDirectory() throws IOException
    {
        Path baseDir = getRandomDirPath();
        Path groupDir = baseDir.resolve("groupdir");
        Path templateFile = groupDir.resolve("foo.st");
        writeFile(templateFile, "foo() ::= <<foo>>\n");

        Context context = loader.getGroup()
            .fromDirectory(groupDir.toFile())
            .build()
            .getTemplate("foo")
            .createContext();

        assertRenderingResult("foo", context);
    }

    @Test
    void testGroupFileInDir() throws IOException
    {
        String dir = getRandomDir();
        writeFile(dir, "group.stg", "a() ::= <<awesome>>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("awesome", group, "/group/a");
    }
}
