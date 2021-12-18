package org.puretemplate;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

@Slf4j
class TestIndentation2 extends BaseTest
{
    @Test
    void testIndentInFrontOfTwoExpr() throws IOException
    {
        String templates = "list(a,b) ::= <<" + "  <a><b>" + NEWLINE + ">>" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        ST t = group.getInstanceOf("list");
        t.impl.dump(log::info);
        t.add("a", "Terence");
        t.add("b", "Jim");
        assertRenderingResult("  TerenceJim", t);
    }
}
