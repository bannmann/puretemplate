package org.puretemplate;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestIndentation2 extends BaseTest
{
    @Test
    void testIndentInFrontOfTwoExpr() throws IOException
    {
        String templates = "list(a,b) ::= <<" + "  <a><b>" + NEWLINE + ">>" + NEWLINE;

        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        ST t = group.getInstanceOf("list");
        t.impl.dump();
        t.add("a", "Terence");
        t.add("b", "Jim");
        assertRenderingResult("  TerenceJim", t);
    }
}
