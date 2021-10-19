package org.puretemplate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.antlr.runtime.Token;
import org.junit.jupiter.api.Test;

class TestFormalArgument
{
    private static final FormalArgument FOO = new FormalArgument("foo");
    private static final FormalArgument BAR = new FormalArgument("bar");
    private static final FormalArgument BAZ1 = new FormalArgument("baz", Token.SKIP_TOKEN);
    private static final FormalArgument BAZ2 = new FormalArgument("baz", Token.INVALID_TOKEN);

    @Test
    void testEquals()
    {
        assertTrue(FOO.equals(FOO));
        assertTrue(BAR.equals(BAR));
        assertTrue(BAZ1.equals(BAZ1));
        assertTrue(BAZ2.equals(BAZ2));

        assertFalse(FOO.equals(null));
        assertFalse(BAR.equals(null));
        assertFalse(BAZ1.equals(null));
        assertFalse(BAZ2.equals(null));

        assertFalse(FOO.equals(BAR));
        assertFalse(BAR.equals(BAZ1));
        assertFalse(FOO.equals(BAZ1));
        assertFalse(FOO.equals(BAZ2));

        assertTrue(BAZ1.equals(BAZ2));
        assertTrue(BAZ2.equals(BAZ1));
    }
}
