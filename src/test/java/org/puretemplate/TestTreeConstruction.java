package org.puretemplate;

import static org.junit.Assert.assertEquals;

import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.tree.Tree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTreeConstruction extends org.puretemplate.gUnitBase
{
    @BeforeEach
    public void setup()
    {
        lexerClassName = "org.puretemplate.STLexer";
        parserClassName = "org.puretemplate.STParser";
    }

    @Test
    public void test_template1() throws ReflectiveOperationException
    {
        // gunit test on line 16
        RuleReturnScope rstruct = (RuleReturnScope) execParser("template", "<[]>", 16);
        Object actual = ((Tree) rstruct.getTree()).toStringTree();
        Object expecting = "(EXPR [)";
        assertEquals("testing rule template", expecting, actual);
    }

    @Test
    public void test_template2() throws ReflectiveOperationException
    {
        // gunit test on line 17
        RuleReturnScope rstruct = (RuleReturnScope) execParser("template", "<[a,b]>", 17);
        Object actual = ((Tree) rstruct.getTree()).toStringTree();
        Object expecting = "(EXPR ([ a b))";
        assertEquals("testing rule template", expecting, actual);
    }
}
