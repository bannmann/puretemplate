package com.example;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;

public class TestDollarDelimiters extends BaseTest
{
    @Test
    public void testAttr()
    {
        String template = "hi $name$!";

        Context context = loader.getTemplate()
            .fromString(template)
            .withDelimiters('$', '$')
            .build()
            .createContext()
            .add("name", "Ter");

        assertRenderingResult("hi Ter!", context);
    }

    @Test
    public void testParallelMap()
    {
        String templates = "test(names,phones) ::= <<hi $names,phones:{n,p | $n$:$p$;}$>>\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .withDelimiters('$', '$')
            .build()
            .getTemplate("test")
            .createContext()
            .add("names", "Ter")
            .add("names", "Tom")
            .add("names", "Sumana")
            .add("phones", "x5001")
            .add("phones", "x5002")
            .add("phones", "x5003");

        assertRenderingResult("hi Ter:x5001;Tom:x5002;Sumana:x5003;", context);
    }

    @Test
    public void testRefToAnotherTemplateInSameGroup() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a() ::= << <$b()$> >>\n");
        writeFile(dir, "b.st", "b() ::= <<bar>>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .withDelimiters('$', '$')
            .build();

        assertNoArgRenderingResult(" <bar> ", group, "a");
    }

    @Test
    public void testDefaultArgument() throws IOException
    {
        String templates = "method(name) ::= <<" +
            NEWLINE +
            "$stat(name)$" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(name,value=\"99\") ::= \"x=$value$; // $name$\"" +
            NEWLINE;

        Path filePath = getRandomDirPath().resolve("group.stg");
        writeFile(filePath, templates);

        Context context = loader.getGroup()
            .fromFile(filePath)
            .withDelimiters('$', '$')
            .build()
            .getTemplate("method")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("x=99; // foo", context);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/46">antlr/stringtemplate4#46</a>.
     */
    @Test
    public void testDelimitersClause()
    {
        String templates = "delimiters \"$\", \"$\"" +
            NEWLINE +
            "method(name) ::= <<" +
            NEWLINE +
            "$stat(name)$" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(name,value=\"99\") ::= \"x=$value$; // $name$\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("x=99; // foo", context);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/46">antlr/stringtemplate4#46</a>.
     */
    @Test
    public void testDelimitersClauseInGroupString()
    {
        Context context = loader.getGroup()
            .fromString("delimiters \"$\", \"$\"" +
                NEWLINE +
                "method(name) ::= <<" +
                NEWLINE +
                "$stat(name)$" +
                NEWLINE +
                ">>" +
                NEWLINE +
                "stat(name,value=\"99\") ::= \"x=$value$; // $name$\"" +
                NEWLINE)
            .build()
            .getTemplate("method")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("x=99; // foo", context);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/66">antlr/stringtemplate4#66</a>.
     */
    @Test
    public void testImportTemplatePreservesDelimiters() throws IOException
    {
        String groupFile = "group GenerateHtml;" +
            NEWLINE +
            "import \"html.st\"" +
            NEWLINE +
            "entry() ::= <<" +
            NEWLINE +
            "$html()$" +
            NEWLINE +
            ">>" +
            NEWLINE;
        String htmlFile = "html() ::= <<" + NEWLINE + "<table style=\"stuff\">" + NEWLINE + ">>" + NEWLINE;

        Path dir = getRandomDirPath();
        Path groupFilePath = dir.resolve("GenerateHtml.stg");
        writeFile(groupFilePath, groupFile);
        writeFile(dir, "html.st", htmlFile);

        Group group = loader.getGroup()
            .fromFile(groupFilePath)
            .withDelimiters('$', '$')
            .build();

        assertCorrectRenderingOfHtmlAndEntryTemplates(group);
    }

    private void assertCorrectRenderingOfHtmlAndEntryTemplates(Group group)
    {
        // test html template directly
        assertNoArgRenderingResult("<table style=\"stuff\">", group, "html");

        // test from entry template
        assertNoArgRenderingResult("<table style=\"stuff\">", group, "entry");
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/66">antlr/stringtemplate4#66</a>.
     */
    @Test
    public void testImportGroupPreservesDelimiters() throws IOException
    {
        String groupFile = "group GenerateHtml;" +
            NEWLINE +
            "import \"HtmlTemplates.stg\"" +
            NEWLINE +
            "entry() ::= <<" +
            NEWLINE +
            "$html()$" +
            NEWLINE +
            ">>" +
            NEWLINE;
        String htmlFile = "html() ::= <<" + NEWLINE + "<table style=\"stuff\">" + NEWLINE + ">>" + NEWLINE;

        Path dir = getRandomDirPath();
        Path groupFilePath = dir.resolve("GenerateHtml.stg");
        writeFile(groupFilePath, groupFile);
        writeFile(dir, "HtmlTemplates.stg", htmlFile);

        Group group = loader.getGroup()
            .fromFile(groupFilePath)
            .withDelimiters('$', '$')
            .build();

        assertCorrectRenderingOfHtmlAndEntryTemplates(group);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/66">antlr/stringtemplate4#66</a>.
     */
    @Test
    public void testDelimitersClauseOverridesConstructorDelimiters() throws IOException
    {
        String groupFile = "group GenerateHtml;" +
            NEWLINE +
            "delimiters \"$\", \"$\"" +
            NEWLINE +
            "import \"html.st\"" +
            NEWLINE +
            "entry() ::= <<" +
            NEWLINE +
            "$html()$" +
            NEWLINE +
            ">>" +
            NEWLINE;
        String htmlFile = "html() ::= <<" + NEWLINE + "<table style=\"stuff\">" + NEWLINE + ">>" + NEWLINE;

        Path dir = getRandomDirPath();
        Path groupFilePath = dir.resolve("GenerateHtml.stg");
        writeFile(groupFilePath, groupFile);
        writeFile(dir, "html.st", htmlFile);

        Group group = loader.getGroup()
            .fromFile(groupFilePath)
            .withDelimiters('<', '>')
            .build();

        assertCorrectRenderingOfHtmlAndEntryTemplates(group);
    }

    /**
     * This is part of a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/66">antlr/stringtemplate4#66</a>.
     */
    @Test
    public void testDelimitersClauseOverridesInheritedDelimiters() throws IOException
    {
        String groupFile = "group GenerateHtml;" +
            NEWLINE +
            "delimiters \"<\", \">\"" +
            NEWLINE +
            "import \"HtmlTemplates.stg\"" +
            NEWLINE +
            "entry() ::= <<" +
            NEWLINE +
            "<html()>" +
            NEWLINE +
            ">>" +
            NEWLINE;
        String htmlFile = "delimiters \"$\", \"$\"" +
            NEWLINE +
            "html() ::= <<" +
            NEWLINE +
            "<table style=\"stuff\">" +
            NEWLINE +
            ">>" +
            NEWLINE;

        Path dir = getRandomDirPath();
        Path groupFilePath = dir.resolve("GenerateHtml.stg");
        writeFile(groupFilePath, groupFile);
        writeFile(dir, "HtmlTemplates.stg", htmlFile);

        Group group = loader.getGroup()
            .fromFile(groupFilePath)
            .build();

        assertCorrectRenderingOfHtmlAndEntryTemplates(group);
    }
}
