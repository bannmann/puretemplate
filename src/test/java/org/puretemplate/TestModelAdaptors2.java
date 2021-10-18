package org.puretemplate;

import java.io.IOException;

import org.junit.Test;

import com.example.TestModelAdaptors;

public class TestModelAdaptors2 extends TestModelAdaptors
{
    @Test
    public void testWeCanResetAdaptorCacheInvalidatedUponAdaptorReset() throws IOException
    {
        String templates = "foo(x) ::= \"<x.id>: <x.name>\"\n";
        writeFile(tmpdir, "foo.stg", templates);
        STGroup group = STGroupFilePath.createWithDefaults(tmpdir + "/foo.stg");
        group.registerModelAdaptor(User.class, new UserAdaptor());
        group.getModelAdaptor(User.class); // get User, SuperUser into cache
        group.getModelAdaptor(SuperUser.class);

        group.registerModelAdaptor(User.class, new UserAdaptorConst());
        // cache should be reset so we see new adaptor
        ST st = group.getInstanceOf("foo");
        st.add("x", new User(100, "parrt"));
        assertRenderingResult("const id value: const name value", st);
    }
}
