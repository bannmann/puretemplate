package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class TestResources extends BaseTest
{
    @Test
    void testRelativize()
    {
        Path dir = Resources.get("/org/puretemplate");
        Path subdir = Resources.get("/org/puretemplate/groupdir");
        assertEquals("groupdir",
            dir.relativize(subdir)
                .toString());
    }

    @Test
    void testStGroupPath() throws IOException
    {
        ST st = new STGroupDirPath(Resources.get("/com/example/groupdir")).getInstanceOf("foo");
        assertRenderingResult("foo", st);
    }
}
