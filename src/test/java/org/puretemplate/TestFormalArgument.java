package org.puretemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.antlr.runtime.Token;
import org.junit.jupiter.api.Test;

class TestFormalArgument
{
    private static final FormalArgument FOO = new FormalArgument("foo");
    private static final FormalArgument BAR = new FormalArgument("bar");
    private static final FormalArgument BAZ1 = new FormalArgument("baz", Token.SKIP_TOKEN);
    private static final FormalArgument BAZ2 = new FormalArgument("baz", Token.INVALID_TOKEN);

    @Test
    @SuppressWarnings({ "java:S5785", "SimplifiableAssertion", "ConstantConditions" })
    void testEquals()
    {
        assertEquals(FOO, FOO);
        assertEquals(BAR, BAR);
        assertEquals(BAZ1, BAZ1);
        assertEquals(BAZ2, BAZ2);

        // We don't simplify to assertNotEquals() as its 'expected/actual' distinction would obscure our intention
        assertFalse(FOO.equals(null));
        assertFalse(BAR.equals(null));
        assertFalse(BAZ1.equals(null));
        assertFalse(BAZ2.equals(null));

        assertNotEquals(FOO, BAR);
        assertNotEquals(BAR, BAZ1);
        assertNotEquals(FOO, BAZ1);
        assertNotEquals(FOO, BAZ2);

        assertEquals(BAZ1, BAZ2);
        assertEquals(BAZ2, BAZ1);
    }
}
