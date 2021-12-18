package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.puretemplate.error.RuntimeMessage;
import org.puretemplate.exception.NoSuchPropertyException;
import org.puretemplate.misc.ErrorBufferAllErrors;

import com.google.common.collect.ImmutableMap;

@Slf4j
class TestCoreBasics2 extends BaseTest
{
    @Test
    void testNullAttr()
    {
        assertNoArgRenderingResult("hi !", "hi <name>!");
    }

    @Test
    void testAttr()
    {
        Context context = makeTemplateContext("hi <name>!").add("name", "Ter");
        assertRenderingResult("hi Ter!", context);
    }

    @Test
    void testChainAttr()
    {
        Context context = makeTemplateContext("<x>:<names>!").add("names", "Ter")
            .add("names", "Tom")
            .add("x", 1);
        assertRenderingResult("1:TerTom!", context);
    }

    @Test
    void testSetUnknownAttr()
    {
        String templates = "t() ::= <<hi <name>!>>\n";

        Context context = loadGroupFromString(templates).getTemplate("t")
            .createContext();

        String result = null;
        try
        {
            context.add("name", "Ter");
        }
        catch (IllegalArgumentException iae)
        {
            result = iae.getMessage();
        }
        String expected = "no such attribute: name";
        assertEquals(expected, result);
    }

    @Test
    void testMultiAttr()
    {
        Context context = makeTemplateContext("hi <name>!").add("name", "Ter")
            .add("name", "Tom");
        assertRenderingResult("hi TerTom!", context);
    }

    @Test
    void testAttrIsList()
    {
        Context context = makeTemplateContext("hi <name>!");
        List<String> names = List.of("Ter", "Tom");
        context.add("name", names);
        context.add("name", "Sumana");

        assertRenderingResult("hi TerTomSumana!", context);
        assertEquals(2, names.size()); // my names list is still just 2
    }

    @Test
    void testAttrIsArray()
    {
        Context context = makeTemplateContext("hi <name>!");
        String[] names = new String[]{ "Ter", "Tom" };
        context.add("name", names);
        context.add("name", "Sumana");

        assertRenderingResult("hi TerTomSumana!", context);
    }

    @Test
    void testProp()
    {
        String template = "<u.id>: <u.name>"; // checks field and method getter
        Context context = makeTemplateContext(template).add("u", new User(1, "parrt"));
        assertRenderingResult("1: parrt", context);
    }

    @Test
    void testPropWithNoAttr()
    {
        Context context = makeTemplateContext("<foo.a>: <ick>").add("foo", Map.of("a", "b"));
        assertRenderingResult("b: ", context);
    }

    @Test
    void testMapAcrossDictionaryUsesKeys()
    {
        String template = "<foo:{f | <f>}>"; // checks field and method getter
        Context context = makeTemplateContext(template).add("foo", ImmutableMap.of("a", "b", "c", "d"));
        assertRenderingResult("ac", context);
    }

    @Test
    void testSTProp()
    {
        String template = "<t.x>"; // get x attr of template context t

        Context t = makeTemplateContext("<x>").add("x", "Ter");

        Context context = makeTemplateContext(template).add("t", t);

        assertRenderingResult("Ter", context);
    }

    @Test
    void testBooleanISProp()
    {
        String template = "<t.manager>"; // call isManager
        Context context = makeTemplateContext(template).add("t", new User(32, "Ter"));
        assertRenderingResult("true", context);
    }

    @Test
    void testBooleanHASProp()
    {
        String template = "<t.parkingSpot>"; // call hasParkingSpot
        Context context = makeTemplateContext(template).add("t", new User(32, "Ter"));
        assertRenderingResult("true", context);
    }

    @Test
    void testNullAttrProp()
    {
        assertNoArgRenderingResult(": ", "<u.id>: <u.name>");
    }

    @Test
    void testNoSuchProp() throws IOException
    {
        ErrorBufferAllErrors errors = new ErrorBufferAllErrors();
        String template = "<u.qqq>";
        STGroup group = new LegacyBareStGroup();
        group.setListener(errors);
        ST st = new ST(group, template);

        st.add("u", new User(1, "parrt"));
        assertRenderingResult("", st);
        RuntimeMessage msg = (RuntimeMessage) errors.getErrors()
            .get(0);
        NoSuchPropertyException e = (NoSuchPropertyException) msg.getCause();
        assertEquals("org.puretemplate.BaseTest$User.qqq", e.getPropertyName());
    }

    @Test
    void testNullIndirectProp() throws IOException
    {
        ErrorBufferAllErrors errors = new ErrorBufferAllErrors();
        STGroup group = new LegacyBareStGroup();
        group.setListener(errors);
        String template = "<u.(qqq)>";
        ST st = new ST(group, template);
        st.add("u", new User(1, "parrt"));
        st.add("qqq", null);
        assertRenderingResult("", st);
        RuntimeMessage msg = (RuntimeMessage) errors.getErrors()
            .get(0);
        NoSuchPropertyException e = (NoSuchPropertyException) msg.getCause();
        assertEquals("org.puretemplate.BaseTest$User.null", e.getPropertyName());
    }

    @Test
    void testPropConvertsToString() throws IOException
    {
        ErrorBufferAllErrors errors = new ErrorBufferAllErrors();
        STGroup group = new LegacyBareStGroup();
        group.setListener(errors);
        String template = "<u.(name)>";
        ST st = new ST(group, template);
        st.add("u", new User(1, "parrt"));
        st.add("name", 100);
        assertRenderingResult("", st);
        RuntimeMessage msg = (RuntimeMessage) errors.getErrors()
            .get(0);
        NoSuchPropertyException e = (NoSuchPropertyException) msg.getCause();
        assertEquals("org.puretemplate.BaseTest$User.100", e.getPropertyName());
    }

    @Test
    void testInclude() throws IOException
    {
        String template = "load <box()>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "kewl" + NEWLINE + "daddy");
        assertRenderingResult("load kewl" + NEWLINE + "daddy;", st);
    }

    @Test
    void testIncludeWithArg() throws IOException
    {
        String template = "load <box(\"arg\")>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "x", "kewl <x> daddy");
        dump(log, st.impl);
        st.add("name", "Ter");
        assertRenderingResult("load kewl arg daddy;", st);
    }

    @Test
    void testIncludeWithEmptySubtemplateArg() throws IOException
    {
        String template = "load <box({})>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "x", "kewl <x> daddy");
        dump(log, st.impl);
        st.add("name", "Ter");
        assertRenderingResult("load kewl  daddy;", st);
    }

    @Test
    void testIncludeWithArg2() throws IOException
    {
        String template = "load <box(\"arg\", foo())>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "x,y", "kewl <x> <y> daddy");
        st.impl.nativeGroup.defineTemplate("foo", "blech");
        st.add("name", "Ter");
        assertRenderingResult("load kewl arg blech daddy;", st);
    }

    @Test
    void testIncludeWithNestedArgs() throws IOException
    {
        String template = "load <box(foo(\"arg\"))>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "y", "kewl <y> daddy");
        st.impl.nativeGroup.defineTemplate("foo", "x", "blech <x>");
        st.add("name", "Ter");
        assertRenderingResult("load kewl blech arg daddy;", st);
    }

    @Test
    void testDefineTemplate() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("inc", "x", "<x>+1");
        group.defineTemplate("test", "name", "hi <name>!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi TerTomSumana!", st);
    }

    @Test
    void testMap() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("inc", "x", "[<x>]");
        group.defineTemplate("test", "name", "hi <name:inc()>!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi [Ter][Tom][Sumana]!", st);
    }

    @Test
    void testIndirectMap() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("inc", "x", "[<x>]");
        group.defineTemplate("test", "t,name", "<name:(t)()>!");
        ST st = group.getInstanceOf("test");
        st.add("t", "inc");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("[Ter][Tom][Sumana]!", st);
    }

    @Test
    void testParallelMap() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names,phones", "hi <names,phones:{n,p | <n>:<p>;}>");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        st.add("phones", "x5001");
        st.add("phones", "x5002");
        st.add("phones", "x5003");
        assertRenderingResult("hi Ter:x5001;Tom:x5002;Sumana:x5003;", st);
    }

    @Test
    void testParallelMapWith3Versus2Elements() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names,phones", "hi <names,phones:{n,p | <n>:<p>;}>");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        st.add("phones", "x5001");
        st.add("phones", "x5002");
        assertRenderingResult("hi Ter:x5001;Tom:x5002;Sumana:;", st);
    }

    @Test
    void testParallelMapThenMap() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("bold", "x", "[<x>]");
        group.defineTemplate("test", "names,phones", "hi <names,phones:{n,p | <n>:<p>;}:bold()>");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        st.add("phones", "x5001");
        st.add("phones", "x5002");
        assertRenderingResult("hi [Ter:x5001;][Tom:x5002;][Sumana:;]", st);
    }

    @Test
    void testMapThenParallelMap() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("bold", "x", "[<x>]");
        group.defineTemplate("test", "names,phones", "hi <[names:bold()],phones:{n,p | <n>:<p>;}>");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        st.add("phones", "x5001");
        st.add("phones", "x5002");
        assertRenderingResult("hi [Ter]:x5001;[Tom]:x5002;[Sumana]:;", st);
    }

    @Test
    void testMapIndexes() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("inc", "x,i", "<i>:<x>");
        group.defineTemplate("test", "name", "<name:{n|<inc(n,i)>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", null); // don't count this one
        st.add("name", "Sumana");
        assertRenderingResult("1:Ter, 2:Tom, 3:Sumana", st);
    }

    @Test
    void testMapIndexes2() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name:{n | <i>:<n>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", null); // don't count this one. still can't apply subtemplate to null value
        st.add("name", "Sumana");
        assertRenderingResult("1:Ter, 2:Tom, 3:Sumana", st);
    }

    @Test
    void testMapSingleValue() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("a", "x", "[<x>]");
        group.defineTemplate("test", "name", "hi <name:a()>!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        assertRenderingResult("hi [Ter]!", st);
    }

    @Test
    void testMapNullValue() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("a", "x", "[<x>]");
        group.defineTemplate("test", "name", "hi <name:a()>!");
        ST st = group.getInstanceOf("test");
        assertRenderingResult("hi !", st);
    }

    @Test
    void testMapNullValueInList() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", null); // don't print this one
        st.add("name", "Sumana");
        assertRenderingResult("Ter, Tom, Sumana", st);
    }

    static Arguments[] mapTestData()
    {
        return new Arguments[]{
            args("RepeatedMap", "hi <name:a():b()>!", "Tom", "hi ([Ter])([Tom])([Sumana])!"),
            args("RepeatedMapWithNullValue", "hi <name:a():b()>!", null, "hi ([Ter])([Sumana])!"),
            args("RepeatedMapWithNullValueAndNullOption",
                "hi <name:a():b(); null={x}>!",
                null,
                "hi ([Ter])x([Sumana])!"),
            args("RoundRobinMap", "hi <name:a(),b()>!", "Tom", "hi [Ter](Tom)[Sumana]!")
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("mapTestData")
    void testMap(
        @SuppressWarnings("unused") String testName,
        String templateSource,
        String middleAttributeValue,
        String expectedResult) throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("a", "x", "[<x>]");
        group.defineTemplate("b", "x", "(<x>)");
        group.defineTemplate("test", "name", templateSource);
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", middleAttributeValue);
        st.add("name", "Sumana");
        assertRenderingResult(expectedResult, st);
    }

    @Test
    void testCharLiterals()
    {
        ST st = new ST("Foo <\\n><\\n><\\t> bar\n");
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        String result = sw.toString();
        String expecting = "Foo \n\n\t bar\n";     // expect \n in output
        assertEquals(expecting, result);

        st = new ST("Foo <\\n><\\t> bar" + NEWLINE);
        sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        expecting = "Foo \n\t bar\n";     // expect \n in output
        result = sw.toString();
        assertEquals(expecting, result);

        st = new ST("Foo<\\ >bar<\\n>");
        sw = new StringWriter();
        st.write(new AutoIndentWriter(sw, "\n")); // force \n as newline
        result = sw.toString();
        expecting = "Foo bar\n"; // forced \n
        assertEquals(expecting, result);
    }

    @Test
    void testSeparator() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        assertRenderingResult("case Ter, case Tom", st);
    }

    @Test
    void testSeparatorInList() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("names", List.of("Ter", "Tom"));
        assertRenderingResult("case Ter, case Tom", st);
    }

    @Test
    void testSeparatorInList2() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", List.of("Tom", "Sriram"));
        assertRenderingResult("case Ter, case Tom, case Sriram", st);
    }

    @Test
    void testSeparatorInArray() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("names", new String[]{ "Ter", "Tom" });
        assertRenderingResult("case Ter, case Tom", st);
    }

    @Test
    void testSeparatorInArray2() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", new String[]{ "Tom", "Sriram" });
        assertRenderingResult("case Ter, case Tom, case Sriram", st);
    }

    @Test
    void testSeparatorInPrimitiveArray() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("names", new int[]{ 0, 1 });
        assertRenderingResult("case 0, case 1", st);
    }

    @Test
    void testSeparatorInPrimitiveArray2() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
        ST st = group.getInstanceOf("test");
        st.add("names", 0);
        st.add("names", new int[]{ 1, 2 });
        assertRenderingResult("case 0, case 1, case 2", st);
    }

    /**
     * (...) forces early eval to string. early eval {@code <(x)>} using new STWriter derived from type of current
     * STWriter. e.g., AutoIndentWriter.
     */
    @Test
    void testEarlyEvalIndent()
    {
        String templates = "t() ::= <<  abc>>\n" + "main() ::= <<\n" + "<t()>\n" + "<(t())>\n" +
            // early eval ignores indents; mostly for simply strings
            "  <t()>\n" + "  <(t())>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        ST st = group.getInstanceOf("main");
        String result = st.render();
        String expected = "  abc" + NEWLINE + "  abc" + NEWLINE + "    abc" + NEWLINE + "    abc";
        assertEquals(expected, result);
    }

    @Test
    void testEarlyEvalNoIndent()
    {
        String templates = "t() ::= <<  abc>>\n" + "main() ::= <<\n" + "<t()>\n" + "<(t())>\n" +
            // early eval ignores indents; mostly for simply strings
            "  <t()>\n" + "  <(t())>\n" + ">>\n";

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        ST st = group.getInstanceOf("main");
        StringWriter sw = new StringWriter();
        NoIndentWriter w = new NoIndentWriter(sw);
        st.write(w);
        String result = sw.toString();
        String expected = "abc" + NEWLINE + "abc" + NEWLINE + "abc" + NEWLINE + "abc";
        assertEquals(expected, result);
    }

    @Test
    void testPrototype() throws IOException
    {
        ST prototype = new ST("simple template");

        ST st = new ST(prototype);
        st.add("arg1", "value");
        assertRenderingResult("simple template", st);

        ST st2 = new ST(prototype);
        st2.add("arg1", "value");
        assertRenderingResult("simple template", st2);
    }
}
