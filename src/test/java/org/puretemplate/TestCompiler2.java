package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.misc.ErrorBuffer;

@Slf4j
class TestCompiler2 extends BaseTest
{
    static Arguments[] anonIncludeArgMismatches()
    {
        return new Arguments[]{
            args("<a:{foo}>", "<string> 1:3: anonymous template has 0 arg(s) but mapped across 1 value(s)"),
            args("<a,b:{x|foo}>", "<string> 1:5: anonymous template has 1 arg(s) but mapped across 2 value(s)"),
            args("<a:{x|foo},{bar}>", "<string> 1:11: anonymous template has 0 arg(s) but mapped across 1 value(s)")
        };
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("anonIncludeArgMismatches")
    void testAnonIncludeArgMismatch(String template, String expectedError)
    {
        ErrorListener errors = new ErrorBuffer();
        loadTemplateFromStringUsingBlankGroup(template, errors);
        assertEquals(expectedError + NEWLINE, errors.toString());
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
        String asmResult = code.getStatementsAsString();
        assertEquals(asmExpected, asmResult);
    }

    @Test
    void testEmbeddedRegion()
    {
        String template = "<@r>foo<@end>";
        // compile as if in root dir and in template 'a'
        CompiledST code = new Compiler().compile("a", template);
        String asmExpected = "new 0 0, write";
        String asmResult = code.getStatementsAsString();
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
        String asmResult = code.getStatementsAsString();
        assertEquals(asmExpected, asmResult);
        String stringsExpected = "[x:, /region__/a__r]";
        String stringsResult = Arrays.toString(code.strings);
        assertEquals(stringsExpected, stringsResult);
    }
}
