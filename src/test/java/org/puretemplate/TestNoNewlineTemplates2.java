package org.puretemplate;

import java.io.IOException;

import org.junit.Test;

public class TestNoNewlineTemplates2 extends BaseTest
{
    @Test
    public void testDefineRegionInSubgroup() throws IOException
    {
        String dir = getRandomDir();
        String g1 = "a() ::= <<[<@r()>]>>\n";
        writeFile(dir, "g1.stg", g1);
        String g2 = "@a.r() ::= <%\n" + "   foo\n\n\n" + "%>\n";
        writeFile(dir, "g2.stg", g2);

        STGroup group1 = STGroupFilePath.createWithDefaults(dir + "/g1.stg");
        STGroup group2 = STGroupFilePath.createWithDefaults(dir + "/g2.stg");
        group2.importTemplates(group1); // define r in g2
        ST st = group2.getInstanceOf("a");
        assertRenderingResult("[foo]", st);
    }
}
