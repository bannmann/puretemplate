package org.puretemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.puretemplate.misc.ErrorBuffer;

class TestGroups extends BaseTest
{
    @Test
    void testSimpleGroup() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= <<foo>>");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("foo", group, "a");
    }

    @Test
    void testEscapeOneRightAngle() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << > >>");

        Context context = loader.getGroup()
            .fromDirectory(dir)
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "parrt");

        assertRenderingResult(" > ", context);
    }

    @Test
    void testEscapeOneRightAngle2() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << \\> >>");

        Context context = loader.getGroup()
            .fromDirectory(dir)
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "parrt");

        assertRenderingResult(" > ", context);
    }

    @Test
    void testEscapeJavaRightShift() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << \\>> >>");

        Context context = loader.getGroup()
            .fromDirectory(dir)
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "parrt");

        assertRenderingResult(" >> ", context);
    }

    @Test
    void testEscapeJavaRightShift2() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= << >\\> >>");

        Context context = loader.getGroup()
            .fromDirectory(dir)
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "parrt");

        assertRenderingResult(" >> ", context);
    }

    @Test
    void testEscapeJavaRightShiftAtRightEdge() throws IOException
    {
        String dir = getRandomDir();
        writeFile(dir, "a.st", "a(x) ::= <<\\>>>"); // <<\>>>

        Context context = loader.getGroup()
            .fromDirectory(dir)
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "parrt");

        assertRenderingResult("\\>", context);
    }

    @Test
    void testEscapeJavaRightShiftAtRightEdge2() throws IOException
    {
        String dir = getRandomDir();
        writeFile(dir, "a.st", "a(x) ::= <<>\\>>>");

        Context context = loader.getGroup()
            .fromDirectory(dir)
            .build()
            .getTemplate("a")
            .createContext()
            .add("x", "parrt");

        assertRenderingResult(">>", context);
    }

    @Test
    void testSimpleGroupFromString()
    {
        String templates = "a(x) ::= <<foo>>\n" + "b() ::= <<bar>>\n";

        Context context = loadGroupFromString(templates).getTemplate("a")
            .createContext();

        assertRenderingResult("foo", context);
    }

    @Test
    void testGroupWithTwoTemplates() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= <<foo>>");
        writeFile(dir, "b.st", "b() ::= \"bar\"");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("foo", group, "a");
        assertNoArgRenderingResult("bar", group, "b");
    }

    @Test
    void testSubdir() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b

        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= <<foo>>");
        writeFile(dir + "/subdir", "b.st", "b() ::= \"bar\"");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("foo", group, "a");
        assertNoArgRenderingResult("bar", group, "/subdir/b");
        assertNoArgRenderingResult("bar", group, "subdir/b");
    }

    @Test
    void testSubdirWithSubtemplate() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b

        Path dir = getRandomDirPath();
        writeFile(dir + "/subdir", "a.st", "a(x) ::= \"<x:{y|<y>}>\"");

        Context context = loader.getGroup()
            .fromDirectory(dir)
            .build()
            .getTemplate("/subdir/a")
            .createContext()
            .add("x", new String[]{ "a", "b" });

        assertRenderingResult("ab", context);
    }

    @Test
    void testGroupFileInDir() throws IOException
    {
        // /randomdir/a and /randomdir/group.stg with b and c templates

        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= <<foo>>");

        String groupFile = "b() ::= \"bar\"\n" + "c() ::= \"duh\"\n";

        writeFile(dir, "group.stg", groupFile);

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("foo", group, "a");
        assertNoArgRenderingResult("bar", group, "/group/b");
        assertNoArgRenderingResult("duh", group, "/group/c");
    }

    @Test
    void testSubSubdir() throws IOException
    {
        // /randomdir/a and /randomdir/subdir/b

        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a(x) ::= <<foo>>");
        writeFile(dir.resolve("sub1/sub2"), "b.st", "b() ::= \"bar\"");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("foo", group, "a");
        assertNoArgRenderingResult("bar", group, "/sub1/sub2/b");
    }

    @Test
    void testGroupFileInSubDir() throws IOException
    {
        // /randomdir/a and /randomdir/sbdir/group.stg with b and c templates

        Path dir = getRandomDirPath();
        System.out.println(dir);
        writeFile(dir, "a.st", "a(x) ::= <<foo>>");
        writeFile(dir.resolve("subdir"), "group.stg", "b() ::= \"bar\"\n" + "c() ::= \"duh\"\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("foo", group, "a");
        assertNoArgRenderingResult("bar", group, "subdir/group/b");
        assertNoArgRenderingResult("duh", group, "subdir/group/c");
    }

    @Test
    void testDupDef() throws IOException
    {
        String templates = "b() ::= \"bar\"\n" + "b() ::= \"duh\"\n";

        ErrorBuffer errors = getGroupLoadingErrors(templates);

        assertEquals("group.stg 2:0: redefinition of template b" + NEWLINE, errors.toString());
    }

    @Test
    void testAlias()
    {
        String templates = "a() ::= \"bar\"\n" + "b ::= a\n";

        Context context = loadGroupFromString(templates).getTemplate("b")
            .createContext();

        assertRenderingResult("bar", context);
    }

    @Test
    void testAliasWithArgs()
    {
        String templates = "a(x,y) ::= \"<x><y>\"\n" + "b ::= a\n";

        Context context = loadGroupFromString(templates).getTemplate("b")
            .createContext()
            .add("x", 1)
            .add("y", 2);

        assertRenderingResult("12", context);
    }

    @Test
    void testSimpleDefaultArg() throws IOException
    {
        Path dir = getRandomDirPath();
        writeFile(dir, "a.st", "a() ::= << <b()> >>\n");
        writeFile(dir, "b.st", "b(x=\"foo\") ::= \"<x>\"\n");

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult(" foo ", group, "a");
    }

    @Test
    void testDefaultArgument()
    {
        String templates = "method(name) ::= <<" +
            NEWLINE +
            "<stat(name)>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(name,value=\"99\") ::= \"x=<value>; // <name>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("x=99; // foo", context);
    }

    @Test
    void testBooleanDefaultArguments()
    {
        String templates = "method(name) ::= <<" +
            NEWLINE +
            "<stat(name)>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(name,x=true,y=false) ::= \"<name>; <x> <y>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("foo; true false", context);
    }

    @Test
    void testDefaultArgument2()
    {
        String templates = "stat(name,value=\"99\") ::= \"x=<value>; // <name>\"" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("stat")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("x=99; // foo", context);
    }

    @Test
    void testSubtemplateAsDefaultArgSeesOtherArgs()
    {
        String templates = "t(x,y={<x:{s|<s><z>}>},z=\"foo\") ::= <<\n" + "x: <x>\n" + "y: <y>\n" + ">>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("t")
            .createContext()
            .add("x", "a");

        assertRenderingResult("x: a" + NEWLINE + "y: afoo", context);
    }

    @Test
    void testEarlyEvalOfDefaultArgs()
    {
        String templates = "s(x,y={<(x)>}) ::= \"<x><y>\"\n"; // should see x in def arg

        Context context = loadGroupFromString(templates).getTemplate("s")
            .createContext()
            .add("x", "a");

        assertRenderingResult("aa", context);
    }

    @Test
    void testDefaultArgumentAsSimpleTemplate()
    {
        String templates = "stat(name,value={99}) ::= \"x=<value>; // <name>\"" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("stat")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("x=99; // foo", context);
    }

    @Test
    void testDefaultArgumentManuallySet()
    {
        class Field
        {
            public String name = "parrt";
            public int n = 0;

            @Override
            public String toString()
            {
                return "Field";
            }
        }
        // set arg f manually for stat(f=f)
        String templates = "method(fields) ::= <<" +
            NEWLINE +
            "<fields:{f | <stat(f)>}>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(f,value={<f.name>}) ::= \"x=<value>; // <f.name>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("fields", new Field());

        assertRenderingResult("x=parrt; // parrt", context);
    }

    @Test
    void testDefaultArgumentSeesVarFromDynamicScoping()
    {
        class Field
        {
            public String name = "parrt";
            public int n = 0;

            @Override
            public String toString()
            {
                return "Field";
            }
        }
        String templates = "method(fields) ::= <<" +
            NEWLINE +
            "<fields:{f | <stat()>}>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(value={<f.name>}) ::= \"x=<value>; // <f.name>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("fields", new Field());

        assertRenderingResult("x=parrt; // parrt", context);
    }

    @Test
    void testDefaultArgumentImplicitlySet2()
    {
        class Field
        {
            public String name = "parrt";
            public int n = 0;

            @Override
            public String toString()
            {
                return "Field";
            }
        }
        // f of stat is implicit first arg
        String templates = "method(fields) ::= <<" +
            NEWLINE +
            "<fields:{f | <f:stat()>}>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(f,value={<f.name>}) ::= \"x=<value>; // <f.name>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("fields", new Field());

        assertRenderingResult("x=parrt; // parrt", context);
    }

    @Test
    void testDefaultArgumentAsTemplate()
    {
        String templates = "method(name,size) ::= <<" +
            NEWLINE +
            "<stat(name)>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(name,value={<name>}) ::= \"x=<value>; // <name>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("name", "foo")
            .add("size", "2");

        assertRenderingResult("x=foo; // foo", context);
    }

    @Test
    void testDefaultArgumentAsTemplate2()
    {
        String templates = "method(name,size) ::= <<" +
            NEWLINE +
            "<stat(name)>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(name,value={ [<name>] }) ::= \"x=<value>; // <name>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("name", "foo")
            .add("size", "2");

        assertRenderingResult("x=[foo] ; // foo", context);
    }

    @Test
    void testDoNotUseDefaultArgument()
    {
        String templates = "method(name) ::= <<" +
            NEWLINE +
            "<stat(name,\"34\")>" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "stat(name,value=\"99\") ::= \"x=<value>; // <name>\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("method")
            .createContext()
            .add("name", "foo");

        assertRenderingResult("x=34; // foo", context);
    }

    @Test
    void testDefaultArgumentInParensToEvalEarly()
    {
        class Counter
        {
            int n = 0;

            @Override
            public String toString()
            {
                return String.valueOf(n++);
            }
        }

        String templates = "A(x) ::= \"<B()>\"" + NEWLINE + "B(y={<(x)>}) ::= \"<y> <x> <x> <y>\"" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("A")
            .createContext()
            .add("x", new Counter());

        assertRenderingResult("0 1 2 0", context);
    }

    @Test
    void testTrueFalseArgs()
    {
        String templates = "f(x,y) ::= \"<x><y>\"\n" + "g() ::= \"<f(true,{a})>\"";

        Context context = loadGroupFromString(templates).getTemplate("g")
            .createContext();

        assertRenderingResult("truea", context);
    }

    @Test
    void testNamedArgsInOrder()
    {
        String templates = "f(x,y) ::= \"<x><y>\"\n" + "g() ::= \"<f(x={a},y={b})>\"";

        Context context = loadGroupFromString(templates).getTemplate("g")
            .createContext();

        assertRenderingResult("ab", context);
    }

    @Test
    void testNamedArgsOutOfOrder()
    {
        String templates = "f(x,y) ::= \"<x><y>\"\n" + "g() ::= \"<f(y={b},x={a})>\"";

        Context context = loadGroupFromString(templates).getTemplate("g")
            .createContext();

        assertRenderingResult("ab", context);
    }

    @Test
    void testUnknownNamedArg() throws IOException
    {
        String templates = "f(x,y) ::= \"<x><y>\"\n" + "g() ::= \"<f(x={a},z={b})>\"";
        ErrorBuffer errors = new ErrorBuffer();
        Group group = loadGroupViaDisk(templates, errors);

        renderGroupTemplate(group, "g");

        assertEquals("context [/g] 1:1 attribute z isn't defined" + NEWLINE, errors.toString());
    }

    @Test
    void testMissingNamedArg() throws IOException
    {
        String group = "f(x,y) ::= \"<x><y>\"\n" + "g() ::= \"<f(x={a},{b})>\"";

        ErrorBuffer errors = getGroupLoadingErrors(group);

        assertEquals("group.stg 2:18: mismatched input '{' expecting ELLIPSIS" + NEWLINE, errors.toString());
    }

    @Test
    void testNamedArgsNotAllowInIndirectInclude() throws IOException
    {
        String group = "f(x,y) ::= \"<x><y>\"\n" + "g(name) ::= \"<(name)(x={a},y={b})>\"";
        ErrorBuffer errors = getGroupLoadingErrors(group);

        assertEquals("group.stg 2:22: '=' came as a complete surprise to me" + NEWLINE, errors.toString());
    }

    @Test
    void testCantSeeGroupDirIfGroupFileOfSameName() throws IOException
    {
        String dir = getRandomDir();
        String a = "a() ::= <<dir1 a>>\n";
        writeFile(dir, "group/a.st", a); // can't see this file

        String groupFile = "b() ::= \"group file b\"\n";
        writeFile(dir, "group.stg", groupFile);

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertThrowsExceptionOfType(() -> group.getTemplate("group/a"), IllegalArgumentException.class);  // can't see
    }

    @Test
    void testGroupFileImport() throws IOException
    {
        // /randomdir/group1.stg (a template) and /randomdir/group2.stg with b.
        // group1 imports group2, a includes b
        Path dir = getRandomDirPath();
        Path group1File = dir.resolve("group1.stg");
        Path group2File = dir.resolve("group2.stg");

        writeFile(group1File, "import \"group2.stg\"\n" + "a(x) ::= <<\n" + "foo<b()>\n" + ">>\n");
        writeFile(group2File, "b() ::= \"bar\"\n");

        Group group1 = loader.getGroup()
            .fromFile(group1File)
            .build();

        // Is the imported template b found?
        assertNoArgRenderingResult("bar", group1, "b");

        // Is the include of b() resolved?
        assertNoArgRenderingResult("foobar", group1, "a");

        // Are the correct "ThatCreatedThisInstance" groups assigned
        // FIXME assertEquals("group1", sta.groupThatCreatedThisInstance.getName());
        // FIXME assertEquals("group1", stb.groupThatCreatedThisInstance.getName());

        // Are the correct (native) groups assigned for the templates
        // FIXME assertEquals("group1", sta.impl.nativeGroup.getName());
        // FIXME assertEquals("group2", stb.impl.nativeGroup.getName());
    }

    @Test
    void testGetTemplateNames()
    {
        String templates = "t() ::= \"foo\"\n" + "main() ::= \"<t()>\"";
        writeFile(tmpdir, "t.stg", templates);

        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/t.stg");
        // try to get an undefined template.
        // This will add an entry to the "templates" field in STGroup, however
        // this should not be returned.
        group.lookupTemplate("t2");

        Set<String> names = group.getTemplateNames();

        // Should only contain "t" and "main" (not "t2")
        assertEquals(2, names.size());
        assertTrue(names.contains("/t"));
        assertTrue(names.contains("/main"));
    }

    @Test
    void testLineBreakInGroup()
    {
        String templates = "t() ::= <<" + NEWLINE + "Foo <\\\\>" + NEWLINE + "  \t  bar" + NEWLINE + ">>" + NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("t")
            .createContext();

        assertRenderingResult("Foo bar", context);
    }

    @Test
    void testLineBreakInGroup2()
    {
        String templates = "t() ::= <<" +
            NEWLINE +
            "Foo <\\\\>       " +
            NEWLINE +
            "  \t  bar" +
            NEWLINE +
            ">>" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("t")
            .createContext();

        assertRenderingResult("Foo bar", context);
    }

    @Test
    void testLineBreakMissingTrailingNewline()
    {
        writeFile(tmpdir, "t.stg", "a(x) ::= <<<\\\\>\r\n>>"); // that is <<<\\>>> not an escaped >>
        ErrorBuffer errors = new ErrorBuffer();

        Template template = loader.getGroup()
            .fromFile(tmpdir + "/" + "t.stg")
            .withErrorListener(errors)
            .build()
            .getTemplate("a");

        assertEquals("t.stg 1:15: Missing newline after newline escape <\\\\>" + NEWLINE, errors.toString());

        Context context = template.createContext()
            .add("x", "parrt");

        assertRenderingResult("", context);
    }

    @Test
    void testLineBreakWithScarfedTrailingNewline()
    {
        writeFile(tmpdir, "t.stg", "a(x) ::= <<<\\\\>\r\n>>"); // \r\n removed as trailing whitespace
        ErrorBuffer errors = new ErrorBuffer();

        Template template = loader.getGroup()
            .fromFile(tmpdir + "/" + "t.stg")
            .withErrorListener(errors)
            .build()
            .getTemplate("a");

        assertEquals("t.stg 1:15: Missing newline after newline escape <\\\\>" + NEWLINE, errors.toString());

        Context context = template.createContext()
            .add("x", "parrt");

        assertRenderingResult("", context);
    }

    public void doMultipleThreadInvoke(Callable<Object> task) throws InterruptedException, ExecutionException
    {
        ExecutorService pool = Executors.newFixedThreadPool(20);
        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>(100);
        for (int i = 0; i < 100; i++)
        {
            tasks.add(task);
        }

        List<Future<Object>> futures = pool.invokeAll(tasks);
        pool.shutdown();

        for (Future<Object> future : futures)
        {
            future.get();
        }
    }

    @Test
    void testGroupStringMultipleThreads() throws ExecutionException, InterruptedException
    {
        Group group = loader.getGroup()
            .fromString("stat(name,value={99}) ::= \"x=<value>; // <name>\"" + NEWLINE)
            .build();

        doMultipleThreadInvoke(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                testGroupString(group);
                return null;
            }
        });
    }

    public void testGroupString(Group group) throws Exception
    {
        Context context = group.getTemplate("stat")
            .createContext()
            .add("name", "foo");
        assertRenderingResult("x=99; // foo", context);
    }

    @Test
    void testGroupFileMultipleThreads() throws IOException, ExecutionException, InterruptedException
    {
        // /randomdir/a and /randomdir/group.stg with b and c templates
        String dir = getRandomDir();
        writeFile(dir, "a.stg", "a(x) ::= <<foo>>");

        final STGroup group = STGroupFilePath.createWithDefaults(dir + "/a.stg");

        doMultipleThreadInvoke(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                testGroupFile(group);
                return null;
            }
        });
    }

    private void testGroupFile(STGroup group) throws IOException
    {
        assertTrue(group.isDefined("a"));
        assertRenderingResult("foo", group.getInstanceOf("a"));
    }
}
