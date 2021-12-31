package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.puretemplate.misc.ErrorBuffer;

@Slf4j
class TestDictionaries extends BaseTest
{
    @Test
    void testDict()
    {
        String templates = "typeInit ::= [\"int\":\"0\", \"float\":\"0.0\"] " +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("type", "int")
            .add("name", "x");

        assertRenderingResult("int x = 0;", context);
    }

    @Test
    void testDictValuesAreTemplates()
    {
        String templates = "typeInit ::= [\"int\":{0<w>}, \"float\":{0.0<w>}] " +
            NEWLINE +
            "var(type,w,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Template template = loadGroupFromString(templates).getTemplate("var");
        dump(log, template);

        Context context = template.createContext()
            .add("w", "L")
            .add("type", "int")
            .add("name", "x");
        assertRenderingResult("int x = 0L;", context);
    }

    @Test
    void testDictKeyLookupViaTemplate() throws IOException
    {
        // Make sure we try rendering stuff to string if not found as regular object
        String templates = "typeInit ::= [\"int\":{0<w>}, \"float\":{0.0<w>}] " +
            NEWLINE +
            "var(type,w,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupViaDisk(templates).getTemplate("var")
            .createContext()
            .add("w", "L")
            .add("type", makeTemplateContext("int"))
            .add("name", "x");
        assertRenderingResult("int x = 0L;", context);
    }

    @Test
    void testDictKeyLookupAsNonToStringableObject() throws IOException
    {
        // Make sure we try rendering stuff to string if not found as regular object
        String templates = "foo(m,k) ::= \"<m.(k)>\"" + NEWLINE;
        writeFile(tmpdir, "test.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "test.stg");
        ST st = group.getInstanceOf("foo");
        Map<HashableUser, String> m = new HashMap<HashableUser, String>();
        m.put(new HashableUser(99, "parrt"), "first");
        m.put(new HashableUser(172036, "tombu"), "second");
        m.put(new HashableUser(391, "sriram"), "third");
        st.add("m", m);
        st.add("k", new HashableUser(172036, "tombu"));
        assertRenderingResult("second", st);
    }

    @Test
    void testDictMissingDefaultValueIsEmpty()
    {
        String templates = "typeInit ::= [\"int\":\"0\", \"float\":\"0.0\"] " +
            NEWLINE +
            "var(type,w,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("w", "L")
            .add("type", "double")
            .add("name", "x");

        assertRenderingResult("double x = ;", context);
    }

    @Test
    void testDictMissingDefaultValueIsEmptyForNullKey()
    {
        String templates = "typeInit ::= [\"int\":\"0\", \"float\":\"0.0\"] " +
            NEWLINE +
            "var(type,w,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("w", "L")
            .add("type", null)
            .add("name", "x");

        assertRenderingResult(" x = ;", context);
    }

    @Test
    void testDictHiddenByFormalArg()
    {
        String templates = "typeInit ::= [\"int\":\"0\", \"float\":\"0.0\"] " +
            NEWLINE +
            "var(typeInit,type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("type", "int")
            .add("name", "x");

        assertRenderingResult("int x = ;", context);
    }

    @Test
    void testDictEmptyValueAndAngleBracketStrings()
    {
        String templates = "typeInit ::= [\"int\":\"0\", \"float\":, \"double\":<<0.0L>>] " +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("type", "float")
            .add("name", "x");

        assertRenderingResult("float x = ;", context);
    }

    @Test
    void testDictDefaultValue()
    {
        String templates = "typeInit ::= [\"int\":\"0\", default:\"null\"] " +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("type", "UserRecord")
            .add("name", "x");

        assertRenderingResult("UserRecord x = null;", context);
    }

    @Test
    void testDictNullKeyGetsDefaultValue()
    {
        String templates = "typeInit ::= [\"int\":\"0\", default:\"null\"] " +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("name", "x");

        assertRenderingResult(" x = null;", context);
    }

    @Test
    void testDictEmptyDefaultValue()
    {
        String templates = "typeInit ::= [\"int\":\"0\", default:] " +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;
        writeFile(tmpdir, "test.stg", templates);
        ErrorBuffer errors = new ErrorBuffer();
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "test.stg");
        group.setListener(errors);
        group.load();
        assertEquals("[test.stg 1:33: missing value for key at ']']",
            errors.getErrors()
                .toString());
    }

    @Test
    void testDictDefaultValueIsKey()
    {
        String templates = "typeInit ::= [\"int\":\"0\", default:key] " +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("type", "UserRecord")
            .add("name", "x");

        assertRenderingResult("UserRecord x = UserRecord;", context);
    }

    /**
     * Test that a map can have only the default entry.
     */
    @Test
    void testDictDefaultStringAsKey()
    {
        String templates = "typeInit ::= [\"default\":\"foo\"] " +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("var")
            .createContext()
            .add("type", "default")
            .add("name", "x");

        assertRenderingResult("default x = foo;", context);
    }

    /**
     * Test that a map can return a <b>string</b> with the word: default.
     */
    @Test
    void testDictDefaultIsDefaultString() throws IOException
    {
        String templates = "map ::= [default: \"default\"] " + NEWLINE + "t() ::= << <map.(\"1\")> >>" + NEWLINE;
        writeFile(tmpdir, "test.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "test.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult(" default ", st);
    }

    @Test
    void testDictViaEnclosingTemplates()
    {
        String templates = "typeInit ::= [\"int\":\"0\", \"float\":\"0.0\"] " +
            NEWLINE +
            "intermediate(type,name) ::= \"<var(type,name)>\"" +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("intermediate")
            .createContext()
            .add("type", "int")
            .add("name", "x");

        assertRenderingResult("int x = 0;", context);
    }

    @Test
    void testDictViaEnclosingTemplates2() throws IOException
    {
        String templates = "typeInit ::= [\"int\":\"0\", \"float\":\"0.0\"] " +
            NEWLINE +
            "intermediate(stuff) ::= \"<stuff>\"" +
            NEWLINE +
            "var(type,name) ::= \"<type> <name> = <typeInit.(type)>;\"" +
            NEWLINE;

        Group group = loadGroupViaDisk(templates);

        Context var = group.getTemplate("var")
            .createContext();
        var.add("type", "int");
        var.add("name", "x");

        Context intermediate = group.getTemplate("intermediate")
            .createContext()
            .add("stuff", var);
        assertRenderingResult("int x = 0;", intermediate);
    }

    @Test
    void TestAccessDictionaryFromAnonymousTemplate() throws IOException
    {
        String dir = tmpdir;
        String g = "a() ::= <<[<[\"foo\",\"a\"]:{x|<if(values.(x))><x><endif>}>]>>\n" +
            "values ::= [\n" +
            "    \"a\":false,\n" +
            "    default:true\n" +
            "]\n";
        writeFile(dir, "g.stg", g);

        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "g.stg");
        ST st = group.getInstanceOf("a");
        assertRenderingResult("[foo]", st);
    }

    @Test
    void TestAccessDictionaryFromAnonymousTemplateInRegion() throws IOException
    {
        String dir = tmpdir;
        String g = "a() ::= <<[<@r()>]>>\n" +
            "@a.r() ::= <<\n" +
            "<[\"foo\",\"a\"]:{x|<if(values.(x))><x><endif>}>\n" +
            ">>\n" +
            "values ::= [\n" +
            "    \"a\":false,\n" +
            "    default:true\n" +
            "]\n";
        writeFile(dir, "g.stg", g);

        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "g.stg");
        ST st = group.getInstanceOf("a");
        assertRenderingResult("[foo]", st);
    }

    @Test
    void testImportDictionary()
    {
        Group root = loader.getGroup()
            .fromString("d ::= [\"a\":\"b\"]\n")
            .build();

        Group sub = loader.getGroup()
            .fromString("t() ::= <<\n" + "<d.a>\n" + ">>\n")
            .importTemplates(root)
            .build();

        assertEquals("b", renderGroupTemplate(sub, "t")); // template 't' is only visible if we can see inherited dicts
    }

    @Test
    void testStringsInDictionary() throws IOException
    {
        String templates = "auxMap ::= [\n" +
            "   \"E\": \"electric <field>\",\n" +
            "   \"I\": \"in <field> between\",\n" +
            "   \"F\": \"<field> force\",\n" +
            "   default: \"<field>\"\n" +
            "]\n" +
            "\n" +
            "makeTmpl(type, field) ::= <<\n" +
            "<auxMap.(type)>\n" +
            ">>\n" +
            "\n" +
            "top() ::= <<\n" +
            "  <makeTmpl(\"E\", \"foo\")>\n" +
            "  <makeTmpl(\"F\", \"foo\")>\n" +
            "  <makeTmpl(\"I\", \"foo\")>\n" +
            ">>\n";
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("top");
        assertNotNull(st);
        assertRenderingResult("  electric <field>" + NEWLINE + "  <field> force" + NEWLINE + "  in <field> between",
            st);
    }

    @Test
    void testTemplatesInDictionary() throws IOException
    {
        String templates = "auxMap ::= [\n" +
            "   \"E\": {electric <field>},\n" +
            "   \"I\": {in <field> between},\n" +
            "   \"F\": {<field> force},\n" +
            "   default: {<field>}\n" +
            "]\n" +
            "\n" +
            "makeTmpl(type, field) ::= <<\n" +
            "<auxMap.(type)>\n" +
            ">>\n" +
            "\n" +
            "top() ::= <<\n" +
            "  <makeTmpl(\"E\", \"foo\")>\n" +
            "  <makeTmpl(\"F\", \"foo\")>\n" +
            "  <makeTmpl(\"I\", \"foo\")>\n" +
            ">>\n";
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("top");
        assertNotNull(st);
        assertRenderingResult("  electric foo" + NEWLINE + "  foo force" + NEWLINE + "  in foo between", st);
    }

    @Test
    void testDictionaryBehaviorTrue() throws IOException
    {
        String templates = "d ::= [\n" +
            "   \"x\" : true,\n" +
            "   default : false,\n" +
            "]\n" +
            "\n" +
            "t() ::= <<\n" +
            "<d.(\"x\")><if(d.(\"x\"))>+<else>-<endif>\n" +
            ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("true+", st);
    }

    @Test
    void testDictionaryBehaviorFalse() throws IOException
    {
        String templates = "d ::= [\n" +
            "   \"x\" : false,\n" +
            "   default : false,\n" +
            "]\n" +
            "\n" +
            "t() ::= <<\n" +
            "<d.(\"x\")><if(d.(\"x\"))>+<else>-<endif>\n" +
            ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("false-", st);
    }

    @Test
    void testDictionaryBehaviorEmptyTemplate() throws IOException
    {
        String templates = "d ::= [\n" +
            "   \"x\" : {},\n" +
            "   default : false,\n" +
            "]\n" +
            "\n" +
            "t() ::= <<\n" +
            "<d.(\"x\")><if(d.(\"x\"))>+<else>-<endif>\n" +
            ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("+", st);
    }

    @Test
    void testDictionaryBehaviorEmptyList() throws IOException
    {
        String templates = "d ::= [\n" +
            "   \"x\" : [],\n" +
            "   default : false\n" +
            "]\n" +
            "\n" +
            "t() ::= <<\n" +
            "<d.(\"x\")><if(d.(\"x\"))>+<else>-<endif>\n" +
            ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("-", st);
    }

    /**
     * This is a regression test for <a href="https://github.com/antlr/stringtemplate4/issues/114">antlr/stringtemplate4#114</a>.
     * Before the fix the following test would return %hi%
     */
    @Test
    void testDictionaryBehaviorNoNewlineTemplate() throws IOException
    {
        String templates = "d ::= [\n" + "   \"x\" : <%hi%>\n" + "]\n" + "\n" + "t() ::= <<\n" + "<d.x>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");
        ST st = group.getInstanceOf("t");
        assertRenderingResult("hi", st);
    }

    @Test
    void testDictionarySpecialValues() throws IOException
    {
        String templates = "t(id) ::= <<\n" +
            "<identifier.(id)>\n" +
            ">>\n" +
            "\n" +
            "identifier ::= [\n" +
            "   \"keyword\" : \"@keyword\",\n" +
            "   default : key\n" +
            "]\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");

        // try with mapped values
        ST template = group.getInstanceOf("t")
            .add("id", "keyword");
        assertRenderingResult("@keyword", template);

        // try with non-mapped values
        template = group.getInstanceOf("t")
            .add("id", "nonkeyword");
        assertRenderingResult("nonkeyword", template);

        // try with non-mapped values that might break (Substring here guarantees unique instances)
        template = group.getInstanceOf("t")
            .add("id", "_default".substring(1));
        assertRenderingResult("default", template);

        template = group.getInstanceOf("t")
            .add("id", "_keys".substring(1));
        assertRenderingResult("keyworddefault", template);

        template = group.getInstanceOf("t")
            .add("id", "_values".substring(1));
        assertRenderingResult("@keywordkey", template);
    }

    @Test
    void testDictionarySpecialValuesOverride() throws IOException
    {
        String templates = "t(id) ::= <<\n" +
            "<identifier.(id)>\n" +
            ">>\n" +
            "\n" +
            "identifier ::= [\n" +
            "   \"keyword\" : \"@keyword\",\n" +
            "   \"keys\" : \"keys\",\n" +
            "   \"values\" : \"values\",\n" +
            "   default : key\n" +
            "]\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + File.separatorChar + "t.stg");

        // try with mapped values
        ST template = group.getInstanceOf("t")
            .add("id", "keyword");
        assertRenderingResult("@keyword", template);

        // try with non-mapped values
        template = group.getInstanceOf("t")
            .add("id", "nonkeyword");
        assertRenderingResult("nonkeyword", template);

        // try with non-mapped values that might break (Substring here guarantees unique instances)
        template = group.getInstanceOf("t")
            .add("id", "_default".substring(1));
        assertRenderingResult("default", template);

        template = group.getInstanceOf("t")
            .add("id", "_keys".substring(1));
        assertRenderingResult("keys", template);

        template = group.getInstanceOf("t")
            .add("id", "_values".substring(1));
        assertRenderingResult("values", template);
    }
}
