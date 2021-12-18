package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.misc.ErrorBuffer;

@Slf4j
class TestCompiler2 extends BaseTest
{
    @Test
    void testAnonIncludeArgMismatch()
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
    void testAnonIncludeArgMismatch2()
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
    void testAnonIncludeArgMismatch3()
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
    void testOptions()
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
    void testEmbeddedRegion()
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
    void testRegion()
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
