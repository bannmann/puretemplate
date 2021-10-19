package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.puretemplate.misc.ErrorBuffer;

class TestScopes2 extends BaseTest
{
    @Test
    void testIndexAttrVisibleLocallyOnly()
    {
        String templates = "t(names) ::= \"<names:{n | <u(n)>}>\"\n" + "u(x) ::= \"<i>:<x>\"";
        ErrorBuffer errors = new ErrorBuffer();
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/" + "t.stg");
        group.setListener(errors);
        ST st = group.getInstanceOf("t");
        st.add("names", "Ter");
        String result = st.render();
        group.getInstanceOf("u").impl.dump();

        String expectedError = "t.stg 2:11: implicitly-defined attribute i not visible" + NEWLINE;
        assertEquals(expectedError, errors.toString());

        String expected = ":Ter";
        assertEquals(expected, result);
    }
}
