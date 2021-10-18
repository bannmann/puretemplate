package com.example;

import java.util.Map;

import org.junit.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;

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

        Context context = loadGroupFromString(templates).getTemplate("main")
            .createContext();

        assertRenderingResult("-ax-*-ay-", context);
    }

    /**
     * @see <a href="http://www.antlr3.org/pipermail/stringtemplate-interest/2011-May/003476.html">stringtemplate-interest
     * post 3476</a>
     */
    @Test
    public void testEarlyEval2()
    {
        String templates = "main() ::= <<\n<f(p=\"x\")>*\n>>\n\n" + "f(p,q={<({a<p>})>}) ::= <<\n-<q>-\n>>";

        Context context = loadGroupFromString(templates).getTemplate("main")
            .createContext();

        assertRenderingResult("-ax-*", context);
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

        Context context = loadGroupFromString(templates).getTemplate("main")
            .createContext();

        assertRenderingResult("Hello", context);
    }

    @Test
    public void testEarlyEvalInIfExpr()
    {
        Context context = loadGroupFromString("main(x) ::= << <if((x))>foo<else>bar<endif> >>").getTemplate("main")
            .createContext();

        assertRenderingResult(" bar ", context);

        context.add("x", "true");

        assertRenderingResult(" foo ", context);
    }

    @Test
    public void testEarlyEvalOfSubtemplateInIfExpr()
    {
        Context
            context
            = loadGroupFromString("main(x) ::= << <if(({a<x>b}))>foo<else>bar<endif> >>").getTemplate("main")
            .createContext();

        assertRenderingResult(" foo ", context);
    }

    @Test
    public void testEarlyEvalOfMapInIfExpr()
    {
        String templates = "m ::= [\n" +
            "   \"parrt\": \"value\",\n" +
            "   default: \"other\"\n" +
            "]\n" +
            "main(x) ::= << p<x>t: <m.({p<x>t})>, <if(m.({p<x>t}))>if<else>else<endif> >>\n";

        Context context = loadGroupFromString(templates).getTemplate("main")
            .createContext()
            .add("x", null);

        assertRenderingResult(" pt: other, if ", context);

        context.add("x", "arr");

        assertRenderingResult(" parrt: value, if ", context);
    }

    @Test
    public void testEarlyEvalOfMapInIfExprPassInHashMap()
    {
        String templates = "main(m,x) ::= << p<x>t: <m.({p<x>t})>, <if(m.({p<x>t}))>if<else>else<endif> >>\n";

        Context context = loadGroupFromString(templates).getTemplate("main")
            .createContext()
            .add("m", Map.of("parrt", "value"))
            .add("x", null);
        assertRenderingResult(" pt: , else ", context);

        context.add("x", "arr");
        assertRenderingResult(" parrt: value, if ", context);
    }
}
