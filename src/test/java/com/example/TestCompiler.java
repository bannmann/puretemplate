package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.puretemplate.BaseTest;
import org.puretemplate.diagnostics.TemplateDiagnostics;

@Slf4j
class TestCompiler extends BaseTest
{
    private static Arguments args(String testName, String template, String asmExpected, String stringsExpected)
    {
        return Arguments.of(testName, template, asmExpected, stringsExpected);
    }

    static Arguments[] bulkTestData()
    {
        return new Arguments[]{
            args("Attr", "hi <name>", "write_str 0, " + "load_attr 1, " + "write", "[hi , name]"),
            args("Include", "hi <foo()>", "write_str 0, new 1 0, write", "[hi , foo]"),
            args("IncludeWithPassThrough",
                "hi <foo(...)>",
                "write_str 0, args, passthru 1, new_box_args 1, write",
                "[hi , foo]"),
            args("IncludeWithPartialPassThrough",
                "hi <foo(x=y,...)>",
                "write_str 0, args, load_attr 1, store_arg 2, passthru 3, new_box_args 3, write",
                "[hi , y, x, foo]"),
            args("SuperInclude", "<super.foo()>", "super_new 0 0, write", "[foo]"),
            args("SuperIncludeWithArgs",
                "<super.foo(a,{b})>",
                "load_attr 0, new 1 0, super_new 2 2, write",
                "[a, _sub1, foo]"),
            args("SuperIncludeWithNamedArgs",
                "<super.foo(x=a,y={b})>",
                "args, load_attr 0, store_arg 1, new 2 0, store_arg 3, super_new_box_args 4, write",
                "[a, x, _sub1, y, foo]"),
            args("IncludeWithArgs",
                "hi <foo(a,b)>",
                "write_str 0, load_attr 1, load_attr 2, new 3 2, write",
                "[hi , a, b, foo]"),
            args("AnonIncludeArgs", "<({ a, b | <a><b>})>", "new 0 0, tostr, write", "[_sub1]"),
            args("IndirectIncludeWitArgs",
                "hi <(foo)(a,b)>",
                "write_str 0, load_attr 1, tostr, load_attr 2, load_attr 3, new_ind 2, write",
                "[hi , foo, a, b]"),
            args("Prop", "hi <a.b>", "write_str 0, load_attr 1, load_prop 2, write", "[hi , a, b]"),
            args("Prop2",
                "<u.id>: <u.name>",
                "load_attr 0, load_prop 1, write, write_str 2, " + "load_attr 0, load_prop 3, write",
                "[u, id, : , name]"),
            args("Map", "<name:bold()>", "load_attr 0, null, new 1 1, map, write", "[name, bold]"),
            args("MapAsOption",
                "<a; wrap=name:bold()>",
                "load_attr 0, options, load_attr 1, null, new 2 1, map, " + "store_option 4, write_opt",
                "[a, name, bold]"),
            args("MapArg", "<name:bold(x)>", "load_attr 0, null, load_attr 1, new 2 2, map, write", "[name, x, bold]"),
            args("IndirectMapArg",
                "<name:(t)(x)>",
                "load_attr 0, load_attr 1, tostr, null, load_attr 2, new_ind 2, map, write",
                "[name, t, x]"),
            args("RepeatedMap",
                "<name:bold():italics()>",
                "load_attr 0, null, new 1 1, map, null, new 2 1, map, write",
                "[name, bold, italics]"),
            args("RepeatedMapArg",
                "<name:bold(x):italics(x,y)>",
                "load_attr 0, null, load_attr 1, new 2 2, map, " +
                    "null, load_attr 1, load_attr 3, new 4 3, map, write",
                "[name, x, bold, y, italics]"),
            args("RotMap",
                "<name:bold(),italics()>",
                "load_attr 0, null, new 1 1, null, new 2 1, rot_map 2, write",
                "[name, bold, italics]"),
            args("RotMapArg",
                "<name:bold(x),italics()>",
                "load_attr 0, null, load_attr 1, new 2 2, null, new 3 1, rot_map 2, write",
                "[name, x, bold, italics]"),
            args("ZipMap",
                "<names,phones:bold()>",
                "load_attr 0, load_attr 1, null, null, new 2 2, zip_map 2, write",
                "[names, phones, bold]"),
            args("ZipMapArg",
                "<names,phones:bold(x)>",
                "load_attr 0, load_attr 1, null, null, load_attr 2, new 3 3, zip_map 2, write",
                "[names, phones, x, bold]"),
            args("AnonMap", "<name:{n | <n>}>", "load_attr 0, null, new 1 1, map, write", "[name, _sub1]"),
            args("AnonZipMap",
                "<a,b:{x,y | <x><y>}>",
                "load_attr 0, load_attr 1, null, null, new 2 2, zip_map 2, write",
                "[a, b, _sub1]"),
            args("If",
                "go: <if(name)>hi, foo<endif>",
                "write_str 0, load_attr 1, brf 12, write_str 2",
                "[go: , name, hi, foo]"),
            args("IfElse",
                "go: <if(name)>hi, foo<else>bye<endif>",
                "write_str 0, " + "load_attr 1, " + "brf 15, " + "write_str 2, " + "br 18, " + "write_str 3",
                "[go: , name, hi, foo, bye]"),
            args("ElseIf",
                "go: <if(name)>hi, foo<elseif(user)>a user<endif>",
                "write_str 0, " +
                    "load_attr 1, " +
                    "brf 15, " +
                    "write_str 2, " +
                    "br 24, " +
                    "load_attr 3, " +
                    "brf 24, " +
                    "write_str 4",
                "[go: , name, hi, foo, user, a user]"),
            args("ElseIfElse",
                "go: <if(name)>hi, foo<elseif(user)>a user<else>bye<endif>",
                "write_str 0, " +
                    "load_attr 1, " +
                    "brf 15, " +
                    "write_str 2, " +
                    "br 30, " +
                    "load_attr 3, " +
                    "brf 27, " +
                    "write_str 4, " +
                    "br 30, " +
                    "write_str 5",
                "[go: , name, hi, foo, user, a user, bye]"),
            args("Option",
                "hi <name; separator=\"x\">",
                "write_str 0, load_attr 1, options, load_str 2, store_option 3, write_opt",
                "[hi , name, x]"),
            args("OptionAsTemplate",
                "hi <name; separator={, }>",
                "write_str 0, load_attr 1, options, new 2 0, store_option 3, write_opt",
                "[hi , name, _sub1]"),
            args("EmptyList", "<[]>", "list, write", "[]"),
            args("List", "<[a,b]>", "list, load_attr 0, add, load_attr 1, add, write", "[a, b]")
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("bulkTestData")
    void testBulk(
        @SuppressWarnings("unused") String testName, String templateSource, String asmExpected, String stringsExpected)
    {
        TemplateDiagnostics diagnostics = loader.getTemplate()
            .fromString(templateSource)
            .build()
            .diagnostics();

        dump(log, diagnostics);

        assertEquals(asmExpected, diagnostics.getInstructions());
        assertEquals(stringsExpected, diagnostics.getStrings());
    }
}
