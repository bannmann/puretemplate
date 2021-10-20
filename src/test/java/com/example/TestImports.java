package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Group;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.misc.ErrorBuffer;

class TestImports extends BaseTest
{
    @Test
    void testImportDir() throws IOException
    {
        /*
        dir1
            base-import-absolute.stg has a() that imports dir2 with absolute path
        dir2
            a.st
            b.st
         */
        String dir1 = getRandomDir() + "/dir1";
        String dir2 = getRandomDir() + "/dir2";
        writeFile(dir1, "base-import-absolute.stg", "import \"" + dir2 + "\"\n" + "a() ::= <<dir1 a>>\n");
        writeFile(dir2, "a.st", "a() ::= <<dir2 a>>\n");
        writeFile(dir2, "b.st", "b() ::= <<dir2 b>>\n");

        Context context = loader.getGroup()
            .fromFile(dir1 + "/base-import-absolute.stg")
            .build()
            .getTemplate("b") // visible only if import worked
            .createContext();
        assertRenderingResult("dir2 b", context);
    }

    @Test
    void testImportResourceDirRelative()
    {
        /*
         * sub
         *     import-base.stg ➞ has a() and imports ../base via classpath
         * base
         *     a.st
         *     b.st
         */

        Group group = loader.getGroup()
            .fromResourceFile(getClass(), "import-tests/sub/import-base.stg")
            .build();

        assertNoArgRenderingResult("base b", group, "b"); // visible only if import worked
    }

    @Test
    void testImportSiblingGroupResourceFile()
    {
        /*
         * main.stg ➞ imports lib.stg
         * lib.stg
         */

        Group group = loader.getGroup()
            .fromResourceFile(getClass(), "import-tests/main.stg")
            .build();

        assertNoArgRenderingResult("main a calls testing lib bold!", group, "a");
    }

    /**
     * A test for <a href="https://github.com/antlr/stringtemplate4/issues/124">antlr/stringtemplate4#124</a>
     */
    @Test
    void testImportRootResourceFileRelative()
    {
        /*
         * main.stg ➞ imports lib.stg
         * lib.stg
         */

        Group group = loader.getGroup()
            .fromResourceFile("/main.stg")
            .build();

        assertNoArgRenderingResult("main a calls root lib bold!", group, "a");
    }

    @Test
    void testImportResourceFileAbsolute()
    {
        /*
         * net/example
         *     main-with-nonroot-lib.stg ➞ org/puretemplate/import-tests/lib.stg
         * org/puretemplate/import-tests
         *     lib.stg
         */

        Group group = loader.getGroup()
            .fromResourceFile("/net/example/main-with-nonroot-lib.stg")
            .build();

        assertNoArgRenderingResult("main a calls testing lib bold!", group, "a");
    }

    @Test
    void testImportRootResourceFile()
    {
        /*
         * net/example
         *     main-with-root-lib.stg ➞ imports /lib.stg
         *
         * lib.stg
         */

        Group group = loader.getGroup()
            .fromResourceFile("/net/example/main-with-root-lib.stg")
            .build();

        assertNoArgRenderingResult("main a calls root lib bold!", group, "a");
    }

    @Test
    void testImportRelativeDir() throws IOException
    {
        /*
         * dir
         *     base-import-absolute.stg has a() that imports subdir with relative path
         *     subdir
         *         a.st
         *         b.st
         *         c.st
         */
        String dir = getRandomDir();
        String gstr = "import \"subdir\"\n" + // finds subdir in dir
            "a() ::= <<dir1 a>>\n";
        writeFile(dir, "base-import-absolute.stg", gstr);

        writeFile(dir, "subdir/a.st", "a() ::= <<subdir a>>\n");
        writeFile(dir, "subdir/b.st", "b() ::= <<subdir b>>\n");
        writeFile(dir, "subdir/c.st", "c() ::= <<subdir c>>\n");

        Group group = loader.getGroup()
            .fromFile(dir + "/base-import-absolute.stg")
            .build();

        assertEquals("subdir b", renderGroupTemplate(group, "b"));
        assertEquals("subdir c", renderGroupTemplate(group, "c"));
    }

    @Test
    void testImportRelativeDirInJarViaCLASSPATH()
    {
        /*
         * org/foo/templates
         *     base-import-absolute.stg has a() that imports subdir with relative path
         *     subdir
         *         a.st
         *         b.st
         *         c.st
         */
        Group group = loader.getGroup()
            .fromResourceFile(getClass(), "import-tests/import-subdir.stg")
            .build();

        assertNoArgRenderingResult("subdir b", group, "b");
    }

    @Test
    void testEmptyGroupImportGroupFileSameDir() throws IOException
    {
        /*
        dir
            group1.stg      that imports group2.stg in same dir with just filename
            group2.stg      has c()
         */
        String dir = getRandomDir();
        writeFile(dir, "group1.stg", "import \"group2.stg\"\n");
        writeFile(dir, "group2.stg", "c() ::= \"g2 c\"\n");

        Group group1 = loader.getGroup()
            .fromFile(dir + "/group1.stg")
            .build();

        assertEquals("g2 c", renderGroupTemplate(group1, "c"));
    }

    @Test
    void testImportGroupFileSameDir() throws IOException
    {
        /*
        dir
            group1.stg      that imports group2.stg in same dir with just filename
            group2.stg      has c()
         */
        String dir = getRandomDir();
        writeFile(dir, "group1.stg", "import \"group2.stg\"\n" + "a() ::= \"g1 a\"\n" + "b() ::= \"<c()>\"\n");
        writeFile(dir, "group2.stg", "c() ::= \"g2 c\"\n");

        Group group1 = loader.getGroup()
            .fromFile(dir + "/group1.stg")
            .build();

        assertEquals("g2 c", renderGroupTemplate(group1, "c"));
    }

    @Test
    void testImportRelativeGroupFile() throws IOException
    {
        /*
        dir
            group1.stg      that imports group2.stg in same dir with just filename
            subdir
                group2.stg  has c()
         */
        String dir = getRandomDir();
        writeFile(dir, "group1.stg", "import \"subdir/group2.stg\"\n" + "a() ::= \"g1 a\"\n" + "b() ::= \"<c()>\"\n");
        writeFile(dir, "subdir/group2.stg", "c() ::= \"g2 c\"\n");

        Group group1 = loader.getGroup()
            .fromFile(dir + "/group1.stg")
            .build();

        assertEquals("g2 c", renderGroupTemplate(group1, "c"));
    }

    @Test
    void testImportTemplateFileSameDir() throws IOException
    {
        /*
        dir
            group1.stg      (that imports c.st)
            c.st
         */
        String dir = getRandomDir();
        writeFile(dir, "group1.stg", "import \"c.st\"\n" + "a() ::= \"g1 a\"\n" + "b() ::= \"<c()>\"\n");
        writeFile(dir, "c.st", "c() ::= \"c\"\n");

        Group group1 = loader.getGroup()
            .fromFile(dir + "/group1.stg")
            .build();

        assertEquals("c", renderGroupTemplate(group1, "c"));
    }

    @Test
    void testImportRelativeTemplateFile() throws IOException
    {
        /*
        dir
            group1.stg      that imports c.st
            subdir
                c.st
         */
        String dir = getRandomDir();
        writeFile(dir, "group1.stg", "import \"subdir/c.st\"\n" + "a() ::= \"g1 a\"\n" + "b() ::= \"<c()>\"\n");
        writeFile(dir, "subdir/c.st", "c() ::= \"c\"\n");

        Group group1 = loader.getGroup()
            .fromFile(dir + "/group1.stg")
            .build();

        assertEquals("c", renderGroupTemplate(group1, "c"));
    }

    @Test
    void testImportTemplateFromAnotherGroupObject() throws IOException
    {
        /*
        dir1
            a.st
            b.st
        dir2
            a.st
         */
        Path dir1 = getRandomDirPath();
        writeFile(dir1.resolve("a.st"), "a() ::= <<dir1 a>>\n");
        writeFile(dir1.resolve("b.st"), "b() ::= <<dir1 b>>\n");

        Path dir2 = getRandomDirPath();
        writeFile(dir2.resolve("a.st"), "a() ::= << <b()> >>\n");

        Group group1 = loader.getGroup()
            .fromDirectory(dir1)
            .build();

        Group group2 = loader.getGroup()
            .fromDirectory(dir2)
            .importTemplates(group1)
            .build();

        // Render template 'b' which was imported from group1
        assertNoArgRenderingResult("dir1 b", group2, "b");

        // Indirectly render imported template 'b' via the template 'a' of group2. Note that 'a' adds whitespace.
        assertNoArgRenderingResult(" dir1 b ", group2, "a");
    }

    @Test
    void testImportTemplateInGroupFileFromDir() throws IOException
    {
        /*
        dir
            x
                a.st
            y
                group.stg       has b, c
         */
        String dir = getRandomDir();
        writeFile(dir, "y/group.stg", "b() ::= \"group file b\"\n" + "c() ::= \"group file c\"\n");
        writeFile(dir, "x/a.st", "a() ::= << <b()> >>");

        Group fileGroup = loader.getGroup()
            .fromFile(Path.of(dir, "y", "group.stg"))
            .build();

        Group dirGroup = loader.getGroup()
            .fromDirectory(Path.of(dir, "x"))
            .importTemplates(fileGroup)
            .build();

        assertNoArgRenderingResult(" group file b ", dirGroup, "a");
    }

    @Test
    void testImportTemplateInGroupFileFromGroupFile() throws IOException
    {
        Path dir = getRandomDirPath();
        Path groupFile1 = dir.resolve("x/group.stg");
        writeFile(groupFile1, "a() ::= \"g1 a\"\n" + "b() ::= \"<c()>\"\n");

        Path groupFile2 = dir.resolve("y/group.stg");
        writeFile(groupFile2, "b() ::= \"g2 b\"\n" + "c() ::= \"g2 c\"\n");

        Group group2 = loader.getGroup()
            .fromFile(groupFile2)
            .build();

        Group group1 = loader.getGroup()
            .fromFile(groupFile1)
            .importTemplates(group2)
            .build();

        assertNoArgRenderingResult("g2 c", group1, "b");
    }

    @Test
    void testImportTemplateFromSubdir() throws IOException
    {
        // /randomdir/x/subdir/a and /randomdir/y/subdir/b
        String dir = getRandomDir();
        writeFile(dir, "x/subdir/a.st", "a() ::= << </subdir/b()> >>");
        writeFile(dir, "y/subdir/b.st", "b() ::= <<y's subdir/b>>");

        Group group2 = loader.getGroup()
            .fromDirectory(dir + "/y")
            .build();

        Group group1 = loader.getGroup()
            .fromDirectory(dir + "/x")
            .importTemplates(group2)
            .build();

        assertNoArgRenderingResult(" y's subdir/b ", group1, "/subdir/a");
    }

    @Test
    void testImportTemplateFromGroupFile() throws IOException
    {
        // /randomdir/x/subdir/a and /randomdir/y/subdir.stg which has a and b
        String dir = getRandomDir();
        writeFile(dir, "x/subdir/a.st", "a() ::= << </subdir/b()> >>");
        writeFile(dir, "y/subdir.stg", "a() ::= \"group file: a\"\n" + "b() ::= \"group file: b\"\n");

        Group group2 = loader.getGroup()
            .fromDirectory(dir + "/y")
            .build();

        Group group1 = loader.getGroup()
            .fromDirectory(dir + "/x")
            .importTemplates(group2)
            .build();

        assertNoArgRenderingResult(" group file: b ", group1, "/subdir/a");
    }

    @Test
    void testPolymorphicTemplateReference() throws IOException
    {
        String dir1 = getRandomDir();
        writeFile(dir1, "b.st", "b() ::= <<dir1 b>>\n");

        String dir2 = getRandomDir();
        writeFile(dir2, "a.st", "a() ::= << <b()> >>\n");
        writeFile(dir2, "b.st", "b() ::= <<dir2 b>>\n");

        Group group2 = loader.getGroup()
            .fromDirectory(dir2)
            .build();

        Group group1 = loader.getGroup()
            .fromDirectory(dir1)
            .importTemplates(group2)
            .build();

        // normal lookup; 'a' created from dir2 calls dir2.b
        assertNoArgRenderingResult(" dir2 b ", group2, "a");

        // polymorphic lookup; 'a' created from dir1 calls dir2.a which calls dir1.b
        assertNoArgRenderingResult(" dir1 b ", group1, "a");
    }

    @Test
    void testSuper() throws IOException
    {
        String dir1 = getRandomDir();
        writeFile(dir1, "a.st", "a() ::= <<dir1 a>>\n");
        writeFile(dir1, "b.st", "b() ::= <<dir1 b>>\n");

        String dir2 = getRandomDir();
        writeFile(dir2, "a.st", "a() ::= << [<super.a()>] >>\n");

        Group group1 = loader.getGroup()
            .fromDirectory(dir1)
            .build();

        Group group2 = loader.getGroup()
            .fromDirectory(dir2)
            .importTemplates(group1)
            .build();

        assertNoArgRenderingResult(" [dir1 a] ", group2, "a");
    }

    /**
     * Cannot import from a group file unless it's the root.
     */
    @Test
    void testGroupFileInDirImportsAnotherGroupFile() throws IOException
    {
        // /randomdir/group.stg with a() imports /randomdir/imported.stg with b()
        // can't have groupdir then groupfile inside that imports
        String dir = getRandomDir();
        writeFile(dir, "group.stg", "import \"imported.stg\"\n" + "a() ::= \"a: <b()>\"\n");
        writeFile(dir, "imported.stg", "b() ::= \"b\"\n");

        ErrorListener errors = new ErrorBuffer();

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .withErrorListener(errors)
            .build();

        IllegalArgumentException thrown = obtainThrownExceptionOfType(() -> group.getTemplate("/group/a"),
            IllegalArgumentException.class);

        assertTrue(thrown.getMessage()
            .startsWith("Invalid template name /group/a"), "exception mentions template name");

        String result = errors.toString();
        assertTrue(result.contains("import illegal in group files embedded in directory-based groups"));
    }

    @Test
    void testGroupFileInDirImportsAGroupDir() throws IOException
    {
        /*
         * dir
         *     import-subdir.stg ➞ has a() and imports subdir with relative path
         *     subdir
         *         b.st
         *         c.st
         */

        Path dir = getRandomDirPath();

        Path groupFilePath = dir.resolve("import-subdir.stg");
        String gstr = "import \"subdir\"\n" + // finds subdir in dir
            "a() ::= \"a: <b()>\"\n";
        writeFile(groupFilePath, gstr);

        writeFile(dir, "subdir/b.st", "b() ::= \"b: <c()>\"\n");
        writeFile(dir, "subdir/c.st", "c() ::= <<subdir c>>\n");

        Group group = loader.getGroup()
            .fromFile(groupFilePath)
            .build();
        assertNoArgRenderingResult("a: b: subdir c", group, "a");
    }

    @Test
    void testImportUsesStandardUtfCharset() throws IOException
    {
        /*
        dir
            group.stg       (that imports c.st)
            c.st
         */

        // Note: UTF-8 is the standard for both writeFile() and getGroup().

        String dir = getRandomDir();
        writeFile(dir, "group.stg", "import \"c.st\"\n" + "b() ::= \"foo\"\n");
        writeFile(dir, "c.st", "c() ::= \"2πr\"\n");

        Group group = loader.getGroup()
            .fromFile(dir + "/group.stg")
            .build();

        assertEquals("2πr", renderGroupTemplate(group, "c"));
    }

    @Test
    void testImportUsesGroupCharset() throws IOException
    {
        /*
        dir
            group.stg       (that imports c.st)
            c.st
         */
        Path dir = getRandomDirPath();
        writeFile(dir.resolve("group.stg"), "import \"c.st\"\n" + "b() ::= \"foo\"\n");
        writeFile(dir.resolve("c.st"), "c() ::= \"Paragraph¶\"\n", StandardCharsets.ISO_8859_1);

        Group group = loader.getGroup()
            .fromFile(dir + "/group.stg")
            .usingCharset(StandardCharsets.ISO_8859_1)
            .build();

        assertEquals("Paragraph¶", renderGroupTemplate(group, "c"));
    }
}
