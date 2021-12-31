package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.misc.ErrorBuffer;

@Slf4j
class TestRegions extends BaseTest
{
    @Test
    void testEmbeddedRegion() throws IOException
    {
        String dir = getRandomDir();
        String groupFile = "a() ::= <<\n" + "[<@r>bar<@end>]\n" + ">>\n";
        writeFile(dir, "group.stg", groupFile);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/group.stg");
        ST st = group.getInstanceOf("a");
        assertRenderingResult("[bar]", st);
    }

    @Test
    void testRegion() throws IOException
    {
        String dir = getRandomDir();
        String groupFile = "a() ::= <<\n" + "[<@r()>]\n" + ">>\n";
        writeFile(dir, "group.stg", groupFile);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/group.stg");
        ST st = group.getInstanceOf("a");
        assertRenderingResult("[]", st);
    }

    @Test
    void testDefineRegionInSubgroup() throws IOException
    {
        String dir = getRandomDir();
        writeFile(dir, "g1.stg", "a() ::= <<[<@r()>]>>\n");
        writeFile(dir, "g2.stg", "@a.r() ::= <<foo>>\n");

        STGroup group1 = STGroupFilePath.createWithDefaults(dir + "/g1.stg");
        STGroup group2 = STGroupFilePath.createWithDefaults(dir + "/g2.stg");
        group2.importTemplates(group1); // define r in g2
        ST st = group2.getInstanceOf("a");
        assertRenderingResult("[foo]", st);
    }

    @Test
    void testDefineRegionInSubgroupOneInSubdir() throws IOException
    {
        String dir = getRandomDir();
        writeFile(dir, "g1.stg", "a() ::= <<[<@r()>]>>\n");
        writeFile(dir + "/subdir", "g2.stg", "@a.r() ::= <<foo>>\n");

        STGroup group1 = STGroupFilePath.createWithDefaults(dir + "/g1.stg");
        STGroup group2 = STGroupFilePath.createWithDefaults(dir + "/subdir/g2.stg");
        group2.importTemplates(group1); // define r in g2
        ST st = group2.getInstanceOf("a");
        assertRenderingResult("[foo]", st);
    }

    @Test
    void testDefineRegionInSubgroupBothInSubdir() throws IOException
    {
        String dir = getRandomDir();
        writeFile(dir + "/subdir", "g1.stg", "a() ::= <<[<@r()>]>>\n");
        writeFile(dir + "/subdir", "g2.stg", "@a.r() ::= <<foo>>\n");

        STGroup group1 = STGroupFilePath.createWithDefaults(dir + "/subdir/g1.stg");
        STGroup group2 = STGroupFilePath.createWithDefaults(dir + "/subdir/g2.stg");
        group2.importTemplates(group1); // define r in g2
        ST st = group2.getInstanceOf("a");
        assertRenderingResult("[foo]", st);
    }

    @Test
    void testDefineRegionInSubgroupThatRefsSuper() throws IOException
    {
        String dir = getRandomDir();
        String g1 = "a() ::= <<[<@r>foo<@end>]>>\n";
        writeFile(dir, "g1.stg", g1);
        String g2 = "@a.r() ::= <<(<@super.r()>)>>\n";
        writeFile(dir, "g2.stg", g2);

        STGroup group1 = STGroupFilePath.createWithDefaults(dir + "/g1.stg");
        STGroup group2 = STGroupFilePath.createWithDefaults(dir + "/g2.stg");
        group2.importTemplates(group1); // define r in g2
        ST st = group2.getInstanceOf("a");
        assertRenderingResult("[(foo)]", st);
    }

    @Test
    void testDefineRegionInSubgroup2() throws IOException
    {
        String dir = getRandomDir();
        String g1 = "a() ::= <<[<@r()>]>>\n";
        writeFile(dir, "g1.stg", g1);
        String g2 = "@a.r() ::= <<foo>>>\n";
        writeFile(dir, "g2.stg", g2);

        STGroup group1 = STGroupFilePath.createWithDefaults(dir + "/g1.stg");
        STGroup group2 = STGroupFilePath.createWithDefaults(dir + "/g2.stg");
        group1.importTemplates(group2); // opposite of previous; g1 imports g2
        ST st = group1.getInstanceOf("a");
        assertRenderingResult("[]", st);
    }

    @Test
    void testDefineRegionInSameGroup() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= <<[<@r()>]>>\n" + "@a.r() ::= <<foo>>\n";
        writeFile(dir, "g.stg", g);

        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");
        ST st = group.getInstanceOf("a");
        assertRenderingResult("[foo]", st);
    }

    @Test
    void testAnonymousTemplateInRegion() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= <<[<@r()>]>>\n" + "@a.r() ::= <<\n" + "<[\"foo\"]:{x|<x>}>\n" + ">>\n";
        writeFile(dir, "g.stg", g);

        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");
        ST st = group.getInstanceOf("a");
        assertRenderingResult("[foo]", st);
    }

    @Test
    void testAnonymousTemplateInRegionInSubdir() throws IOException
    {
        //fails since it makes region name /region__/g/a/_r
        Path dir = getRandomDirPath();
        String g = "a() ::= <<[<@r()>]>>\n" + "@a.r() ::= <<\n" + "<[\"foo\"]:{x|<x>}>\n" + ">>\n";
        writeFile(dir, "g.stg", g);

        Group group = loader.getGroup()
            .fromDirectory(dir)
            .build();

        assertNoArgRenderingResult("[foo]", group, "g/a");
    }

    @Test
    void testCantDefineEmbeddedRegionAgain() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= <<[<@r>foo<@end>]>>\n" + "@a.r() ::= <<bar>>\n"; // error; dup
        writeFile(dir, "g.stg", g);

        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        group.load();
        assertEquals("g.stg 2:3: region /a.r is embedded and thus already implicitly defined" + NEWLINE,
            errors.toString());
    }

    @Test
    void testCantDefineEmbeddedRegionAgainInTemplate() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= <<\n" + "[\n" + "<@r>foo<@end>\n" + "<@r()>" + "]\n" + ">>\n"; // error; dup
        writeFile(dir, "g.stg", g);

        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        group.load();
        assertEquals("g.stg 3:2: redefinition of region /a.r" + NEWLINE, errors.toString());
    }

    @Test
    void testMissingRegionName() throws IOException
    {
        String dir = getRandomDir();
        String g = "@t.() ::= \"\"\n";
        writeFile(dir, "g.stg", g);

        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");
        ErrorBuffer errors = new ErrorBuffer();
        group.setListener(errors);
        group.load();
        assertEquals("g.stg 1:3: missing ID at '('" + NEWLINE, errors.toString());
    }

    @Test
    void testIndentBeforeRegionIsIgnored() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= <<[\n" + "  <@r>\n" + "  foo\n" + "  <@end>\n" + "]>>\n";
        writeFile(dir, "g.stg", g);

        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");
        ST st = group.getInstanceOf("a");
        assertRenderingResult("[" + NEWLINE + "  foo" + NEWLINE + "]", st);
    }

    @Test
    void testRegionOverrideStripsNewlines() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= \"X<@r()>Y\"" + "@a.r() ::= <<\n" + "foo\n" + ">>\n";
        writeFile(dir, "g.stg", g);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");

        String sub = "@a.r() ::= \"A<@super.r()>B\"" + NEWLINE;
        writeFile(dir, "sub.stg", sub);
        STGroup subGroup = STGroupFilePath.createWithDefaults(dir + "/sub.stg");
        subGroup.importTemplates(group);

        ST st = subGroup.getInstanceOf("a");
        String result = st.render();
        String expecting = "XAfooBY";
        assertEquals(expecting, result);
    }

    //

    @Test
    void testRegionOverrideRefSuperRegion() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= \"X<@r()>Y\"" + "@a.r() ::= \"foo\"" + NEWLINE;
        writeFile(dir, "g.stg", g);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");

        String sub = "@a.r() ::= \"A<@super.r()>B\"" + NEWLINE;
        writeFile(dir, "sub.stg", sub);
        STGroup subGroup = STGroupFilePath.createWithDefaults(dir + "/sub.stg");
        subGroup.importTemplates(group);

        ST st = subGroup.getInstanceOf("a");
        String result = st.render();
        String expecting = "XAfooBY";
        assertEquals(expecting, result);
    }

    @Test
    void testRegionOverrideRefSuperRegion2Levels()
    {
        String g = "a() ::= \"X<@r()>Y\"\n" + "@a.r() ::= \"foo\"\n";
        STGroup group = new STGroupString(g);

        String sub = "@a.r() ::= \"<@super.r()>2\"\n";
        STGroup subGroup = new STGroupString(sub);
        subGroup.importTemplates(group);

        ST st = subGroup.getInstanceOf("a");

        String result = st.render();
        String expecting = "Xfoo2Y";
        assertEquals(expecting, result);
    }

    @Test
    void testRegionOverrideRefSuperRegion3Levels() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= \"X<@r()>Y\"" + "@a.r() ::= \"foo\"" + NEWLINE;
        writeFile(dir, "g.stg", g);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");

        String sub = "@a.r() ::= \"<@super.r()>2\"" + NEWLINE;
        writeFile(dir, "sub.stg", sub);
        STGroup subGroup = STGroupFilePath.createWithDefaults(dir + "/sub.stg");
        subGroup.importTemplates(group);

        String subsub = "@a.r() ::= \"<@super.r()>3\"" + NEWLINE;
        writeFile(dir, "subsub.stg", subsub);
        STGroup subSubGroup = STGroupFilePath.createWithDefaults(dir + "/subsub.stg");
        subSubGroup.importTemplates(subGroup);

        ST st = subSubGroup.getInstanceOf("a");

        String result = st.render();
        String expecting = "Xfoo23Y";
        assertEquals(expecting, result);
    }

    @Test
    void testRegionOverrideRefSuperImplicitRegion() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= \"X<@r>foo<@end>Y\"" + NEWLINE;
        writeFile(dir, "g.stg", g);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");

        String sub = "@a.r() ::= \"A<@super.r()>\"" + NEWLINE;
        writeFile(dir, "sub.stg", sub);
        STGroup subGroup = STGroupFilePath.createWithDefaults(dir + "/sub.stg");
        subGroup.importTemplates(group);

        ST st = subGroup.getInstanceOf("a");
        String result = st.render();
        String expecting = "XAfooY";
        assertEquals(expecting, result);
    }

    @Test
    void testUnknownRegionDefError() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= <<\n" + "X<@r()>Y\n" + ">>\n" + "@a.q() ::= \"foo\"" + NEWLINE;
        ErrorListener errors = new ErrorBuffer();
        writeFile(dir, "g.stg", g);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("a");
        st.render();
        String result = errors.toString();
        String expecting = "g.stg 4:3: template /a doesn't have a region called q" + NEWLINE;
        assertEquals(expecting, result);
    }

    @Test
    void testSuperRegionRefMissingOk() throws IOException
    {
        String dir = getRandomDir();
        String g = "a() ::= \"X<@r()>Y\"" + "@a.r() ::= \"foo\"" + NEWLINE;
        writeFile(dir, "g.stg", g);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/g.stg");

        String sub = "@a.r() ::= \"A<@super.q()>B\"" + NEWLINE; // allow this; trap at runtime
        ErrorListener errors = new ErrorBuffer();
        group.setListener(errors);
        writeFile(dir, "sub.stg", sub);
        STGroup subGroup = STGroupFilePath.createWithDefaults(dir + "/sub.stg");
        subGroup.importTemplates(group);

        ST st = subGroup.getInstanceOf("a");
        String result = st.render();
        String expecting = "XABY";
        assertEquals(expecting, result);
    }

    @Test
    void testEmbeddedRegionOnOneLine()
    {
        String groupFile = "a() ::= <<\n" + "[\n" + "  <@r>bar<@end>\n" + "]\n" + ">>\n";
        Template template = loadGroupFromString(groupFile).getTemplate("a");
        dump(log, template);
        assertRenderingResult("[" + NEWLINE + "  bar" + NEWLINE + "]", template.createContext());
    }

    @Test
    void testEmbeddedRegionTagsOnSeparateLines() throws IOException
    {
        String dir = getRandomDir();
        String groupFile = "a() ::= <<\n" + "[\n" + "  <@r>\n" + "  bar\n" + "  <@end>\n" + "]\n" + ">>\n";
        writeFile(dir, "group.stg", groupFile);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/group.stg");
        ST st = group.getInstanceOf("a");
        assertRenderingResult("[" + NEWLINE + "  bar" + NEWLINE + "]", st);
    }
}
