package org.puretemplate;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.misc.ErrorBuffer;

public class TestCompiler extends BaseTest
{

    @Test
    public void testAttr()
    {
        String template = "hi <name>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, " + "load_attr 1, " + "write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , name]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testInclude()
    {
        String template = "hi <foo()>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, new 1 0, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , foo]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testIncludeWithPassThrough()
    {
        String template = "hi <foo(...)>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, args, passthru 1, new_box_args 1, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , foo]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testIncludeWithPartialPassThrough()
    {
        String template = "hi <foo(x=y,...)>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, args, load_attr 1, store_arg 2, passthru 3, new_box_args 3, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , y, x, foo]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testSuperInclude()
    {
        String template = "<super.foo()>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "super_new 0 0, write";
        code.dump();
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[foo]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testSuperIncludeWithArgs()
    {
        String template = "<super.foo(a,{b})>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, new 1 0, super_new 2 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[a, _sub1, foo]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testSuperIncludeWithNamedArgs()
    {
        String template = "<super.foo(x=a,y={b})>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "args, load_attr 0, store_arg 1, new 2 0, store_arg 3, super_new_box_args 4, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[a, x, _sub1, y, foo]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testIncludeWithArgs()
    {
        String template = "hi <foo(a,b)>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, load_attr 1, load_attr 2, new 3 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , a, b, foo]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testAnonIncludeArgs()
    {
        String template = "<({ a, b | <a><b>})>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "new 0 0, tostr, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[_sub1]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testAnonIncludeArgMismatch()
    {
        ErrorListener errors = new ErrorBuffer();
        String template = "<a:{foo}>";
        STGroup g = new LegacyBareStGroup();
        g.errMgr = new ErrorManager(errors);
        CompiledST code = new Compiler(g).compile(template);
        String expected = "1:3: anonymous template has 0 arg(s) but mapped across 1 value(s)" + NEWLINE;
        assertEquals(expected, errors.toString());
    }

    @Test
    public void testAnonIncludeArgMismatch2()
    {
        ErrorListener errors = new ErrorBuffer();
        String template = "<a,b:{x|foo}>";
        STGroup g = new LegacyBareStGroup();
        g.errMgr = new ErrorManager(errors);
        CompiledST code = new Compiler(g).compile(template);
        String expected = "1:5: anonymous template has 1 arg(s) but mapped across 2 value(s)" + NEWLINE;
        assertEquals(expected, errors.toString());
    }

    @Test
    public void testAnonIncludeArgMismatch3()
    {
        ErrorListener errors = new ErrorBuffer();
        String template = "<a:{x|foo},{bar}>";
        STGroup g = new LegacyBareStGroup();
        g.errMgr = new ErrorManager(errors);
        CompiledST code = new Compiler(g).compile(template);
        String expected = "1:11: anonymous template has 0 arg(s) but mapped across 1 value(s)" + NEWLINE;
        assertEquals(expected, errors.toString());
    }

    @Test
    public void testIndirectIncludeWitArgs()
    {
        String template = "hi <(foo)(a,b)>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, load_attr 1, tostr, load_attr 2, load_attr 3, new_ind 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , foo, a, b]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testProp()
    {
        String template = "hi <a.b>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, load_attr 1, load_prop 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , a, b]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testProp2()
    {
        String template = "<u.id>: <u.name>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, load_prop 1, write, write_str 2, " + "load_attr 0, load_prop 3, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[u, id, : , name]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testMap()
    {
        String template = "<name:bold()>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, null, new 1 1, map, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[name, bold]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testMapAsOption()
    {
        String template = "<a; wrap=name:bold()>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, options, load_attr 1, null, new 2 1, map, " + "store_option 4, write_opt";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[a, name, bold]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testMapArg()
    {
        String template = "<name:bold(x)>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, null, load_attr 1, new 2 2, map, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[name, x, bold]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testIndirectMapArg()
    {
        String template = "<name:(t)(x)>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, load_attr 1, tostr, null, load_attr 2, new_ind 2, map, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[name, t, x]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testRepeatedMap()
    {
        String template = "<name:bold():italics()>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, null, new 1 1, map, null, new 2 1, map, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[name, bold, italics]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testRepeatedMapArg()
    {
        String template = "<name:bold(x):italics(x,y)>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, null, load_attr 1, new 2 2, map, " +
            "null, load_attr 1, load_attr 3, new 4 3, map, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[name, x, bold, y, italics]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testRotMap()
    {
        String template = "<name:bold(),italics()>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, null, new 1 1, null, new 2 1, rot_map 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[name, bold, italics]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testRotMapArg()
    {
        String template = "<name:bold(x),italics()>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, null, load_attr 1, new 2 2, null, new 3 1, rot_map 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[name, x, bold, italics]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testZipMap()
    {
        String template = "<names,phones:bold()>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, load_attr 1, null, null, new 2 2, zip_map 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[names, phones, bold]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testZipMapArg()
    {
        String template = "<names,phones:bold(x)>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, load_attr 1, null, null, load_attr 2, new 3 3, zip_map 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[names, phones, x, bold]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testAnonMap()
    {
        String template = "<name:{n | <n>}>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, null, new 1 1, map, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[name, _sub1]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testAnonZipMap()
    {
        String template = "<a,b:{x,y | <x><y>}>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "load_attr 0, load_attr 1, null, null, new 2 2, zip_map 2, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[a, b, _sub1]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testIf()
    {
        String template = "go: <if(name)>hi, foo<endif>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, load_attr 1, brf 12, write_str 2";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[go: , name, hi, foo]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testIfElse()
    {
        String template = "go: <if(name)>hi, foo<else>bye<endif>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, " +
            "load_attr 1, " +
            "brf 15, " +
            "write_str 2, " +
            "br 18, " +
            "write_str 3";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[go: , name, hi, foo, bye]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testElseIf()
    {
        String template = "go: <if(name)>hi, foo<elseif(user)>a user<endif>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, " +
            "load_attr 1, " +
            "brf 15, " +
            "write_str 2, " +
            "br 24, " +
            "load_attr 3, " +
            "brf 24, " +
            "write_str 4";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[go: , name, hi, foo, user, a user]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testElseIfElse()
    {
        String template = "go: <if(name)>hi, foo<elseif(user)>a user<else>bye<endif>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, " +
            "load_attr 1, " +
            "brf 15, " +
            "write_str 2, " +
            "br 30, " +
            "load_attr 3, " +
            "brf 27, " +
            "write_str 4, " +
            "br 30, " +
            "write_str 5";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[go: , name, hi, foo, user, a user, bye]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testOption()
    {
        String template = "hi <name; separator=\"x\">";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, load_attr 1, options, load_str 2, store_option 3, write_opt";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , name, x]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testOptionAsTemplate()
    {
        String template = "hi <name; separator={, }>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, load_attr 1, options, new 2 0, store_option 3, write_opt";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[hi , name, _sub1]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testOptions()
    {
        String template = "hi <name; anchor, wrap=foo(), separator=\", \">";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "write_str 0, " +
            "load_attr 1, " +
            "options, " +
            "load_str 2, " +
            "store_option 0, " +
            "new 3 0, " +
            "store_option 4, " +
            "load_str 4, " +
            "store_option 3, " +
            "write_opt";
        String stringsExpected = // the ", , ," is the ", " separator string
            "[hi , name, true, foo, , ]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
    }

    @Test
    public void testEmptyList()
    {
        String template = "<[]>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "list, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testList()
    {
        String template = "<[a,b]>";
        CompiledST code = new Compiler().compile(template);
        String asmExpected = "list, load_attr 0, add, load_attr 1, add, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[a, b]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testEmbeddedRegion()
    {
        String template = "<@r>foo<@end>";
        // compile as if in root dir and in template 'a'
        CompiledST code = new Compiler().compile("a", template);
        String asmExpected = "new 0 0, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[/region__/a__r]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }

    @Test
    public void testRegion()
    {
        String template = "x:<@r()>";
        // compile as if in root dir and in template 'a'
        CompiledST code = new Compiler().compile("a", template);
        String asmExpected = "write_str 0, new 1 0, write";
        String asmResult = code.instrs();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[x:, /region__/a__r]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }
}
