package com.github.bannmann.puretemplate;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TestEarlyEvaluation extends BaseTest
{
    /**
     * @see <a href="http://www.antlr3.org/pipermail/stringtemplate-interest/2011-May/003476.html">stringtemplate-interest
     * post 3476</a>
     */
    @Test
    public void testEarlyEval()
    {
        String templates = "main() ::= <<\n<f(p=\"x\")>*<f(p=\"y\")>\n>>\n\n" + "f(p,q={<({a<p>})>}) ::= <<\n-<q>-\n>>";
        writeFile(tmpdir, "t.stg", templates);

        STGroup group = new STGroupFile(tmpdir + "/t.stg");

        ST st = group.getInstanceOf("main");

        String s = st.render();
        Assert.assertEquals("-ax-*-ay-", s);
    }

    /**
     * @see <a href="http://www.antlr3.org/pipermail/stringtemplate-interest/2011-May/003476.html">stringtemplate-interest
     * post 3476</a>
     */
    @Test
    public void testEarlyEval2()
    {
        String templates = "main() ::= <<\n<f(p=\"x\")>*\n>>\n\n" + "f(p,q={<({a<p>})>}) ::= <<\n-<q>-\n>>";
        writeFile(tmpdir, "t.stg", templates);

        STGroup group = new STGroupFile(tmpdir + "/t.stg");

        ST st = group.getInstanceOf("main");

        String s = st.render();
        Assert.assertEquals("-ax-*", s);
    }

    /**
     * @see <a href="http://www.antlr3.org/pipermail/stringtemplate-interest/2011-August/003758.html">stringtemplate-interest
     * post 3758</a>
     */
    @Test
    public void testBugArrayIndexOutOfBoundsExceptionInSTRuntimeMessage_getSourceLocation()
    {
        String templates = "main(doit = true) ::= " +
            "\"<if(doit || other)><t(...)><endif>\"\n" +
            "t2() ::= \"Hello\"\n"
            //
            +
            "t(x={<(t2())>}) ::= \"<x>\"";

        writeFile(tmpdir, "t.stg", templates);

        STGroup group = new STGroupFile(tmpdir + "/t.stg");

        ST st = group.getInstanceOf("main");

        String s = st.render();
        Assert.assertEquals("Hello", s);
    }

    @Test
    public void testEarlyEvalInIfExpr()
    {
        String templates = "main(x) ::= << <if((x))>foo<else>bar<endif> >>";
        writeFile(tmpdir, "t.stg", templates);

        STGroup group = new STGroupFile(tmpdir + "/t.stg");

        ST st = group.getInstanceOf("main");

        String s = st.render();
        Assert.assertEquals(" bar ", s);

        st.add("x", "true");
        s = st.render();
        Assert.assertEquals(" foo ", s);
    }

    @Test
    public void testEarlyEvalOfSubtemplateInIfExpr()
    {
        String templates = "main(x) ::= << <if(({a<x>b}))>foo<else>bar<endif> >>";
        writeFile(tmpdir, "t.stg", templates);

        STGroup group = new STGroupFile(tmpdir + "/t.stg");

        ST st = group.getInstanceOf("main");

        String s = st.render();
        Assert.assertEquals(" foo ", s);
    }

    @Test
    public void testEarlyEvalOfMapInIfExpr()
    {
        String templates = "m ::= [\n" +
            "   \"parrt\": \"value\",\n" +
            "   default: \"other\"\n" +
            "]\n" +
            "main(x) ::= << p<x>t: <m.({p<x>t})>, <if(m.({p<x>t}))>if<else>else<endif> >>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroup group = new STGroupFile(tmpdir + "/t.stg");

        ST st = group.getInstanceOf("main");

        st.add("x", null);
        String s = st.render();
        Assert.assertEquals(" pt: other, if ", s);

        st.add("x", "arr");
        s = st.render();
        Assert.assertEquals(" parrt: value, if ", s);
    }

    @Test
    public void testEarlyEvalOfMapInIfExprPassInHashMap()
    {
        String templates = "main(m,x) ::= << p<x>t: <m.({p<x>t})>, <if(m.({p<x>t}))>if<else>else<endif> >>\n";
        writeFile(tmpdir, "t.stg", templates);

        STGroup group = new STGroupFile(tmpdir + "/t.stg");

        ST st = group.getInstanceOf("main");
        st.add("m", Map.of("parrt", "value"));
        st.add("x", null);

        String s = st.render();
        Assert.assertEquals(" pt: , else ", s); // m[null] has no default value so else clause

        st.add("x", "arr");
        s = st.render();
        Assert.assertEquals(" parrt: value, if ", s);
    }
}
