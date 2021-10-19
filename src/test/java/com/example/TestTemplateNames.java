package com.example;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.misc.ErrorBuffer;

class TestTemplateNames extends BaseTest
{
    @Test
    void testAbsoluteTemplateRefFromOutside() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << </subdir/b()> >>\n");
        writeFile(dir.resolve("subdir"), "b.st", "b() ::= <<bar>>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult(" bar ", group, "a");
        assertNoArgRenderingResult(" bar ", group, "/a");
        assertNoArgRenderingResult("bar", group, "/subdir/b");
    }

    @Test
    void testRelativeTemplateRefInExpr() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << <subdir/b()> >>\n");
        writeFile(dir.resolve("subdir"), "b.st", "b() ::= <<bar>>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult(" bar ", group, "a");
    }

    @Test
    void testAbsoluteTemplateRefInExpr() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << </subdir/b()> >>\n");
        writeFile(dir.resolve("subdir"), "b.st", "b() ::= <<bar>>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult(" bar ", group, "a");
    }

    @Test
    void testRefToAnotherTemplateInSameGroup() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a() ::= << <b()> >>\n");
        writeFile(dir, "b.st", "b() ::= <<bar>>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult(" bar ", group, "a");
    }

    @Test
    void testRefToAnotherTemplateInSameSubdir() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b
        Path dir = getRandomDirPath();
        writeFile(dir.resolve("subdir")
            .resolve("a.st"), "a() ::= << <b()> >>\n");
        writeFile(dir.resolve("subdir")
            .resolve("b.st"), "b() ::= <<bar>>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult(" bar ", group, "/subdir/a");
    }

    @Test
    void testFullyQualifiedGetInstanceOf() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= <<foo>>");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("foo", group, "a");
        assertNoArgRenderingResult("foo", group, "/a");
    }

    @Test
    void testFullyQualifiedTemplateRef() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b
        Path dir = getRandomDirPath();
        writeFile(dir.resolve("subdir"), "a.st", "a() ::= << </subdir/b()> >>\n");
        writeFile(dir.resolve("subdir"), "b.st", "b() ::= <<bar>>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult(" bar ", group, "/subdir/a");
        assertNoArgRenderingResult(" bar ", group, "subdir/a");
    }

    @Test
    void testFullyQualifiedTemplateRef2() throws IOException
    {
        // /randomdir/a and /randomdir/group.stg with b and c templates
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << </group/b()> >>\n");
        writeFile(dir, "group.stg", "b() ::= \"bar\"\n" + "c() ::= \"</a()>\"\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult(" bar ", group, "/a");
        assertNoArgRenderingResult(" bar ", group, "/group/c");  // invokes /a
    }

    @Test
    void testRelativeInSubdir() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << </subdir/c()> >>\n");
        writeFile(dir.resolve("subdir"), "b.st", "b() ::= <<bar>>\n");
        writeFile(dir.resolve("subdir"), "c.st", "c() ::= << <b()> >>\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("  bar  ", group, "a");
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/94">antlr/stringtemplate4#94</a>.
     */
    @Test
    void testIdWithHyphens() throws IOException
    {
        String templates = "template-a(x-1) ::= \"[<x-1>]\"" +
            NEWLINE +
            "template-b(x-2) ::= <<" +
            NEWLINE +
            "<template-a(x-2)>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "t-entry(x-3) ::= <<[<template-b(x-3)>]>>" +
            NEWLINE;

        writeFile(tmpdir, "t.stg", templates);

        ErrorBuffer errors = new ErrorBuffer();
        Context context = loadGroupViaDisk(templates, errors).getTemplate("t-entry")
            .createContext()
            .add("x-3", "x");

        assertRenderingResult("[[x]]", context);

        assertEquals("[]",
            errors.getErrors()
                .toString());
    }

    // TODO: test <a/b()> is RELATIVE NOT ABSOLUTE
}
