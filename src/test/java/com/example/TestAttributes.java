package com.example;

import org.junit.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Template;

public class TestAttributes extends BaseTest
{
    @Test
    public void testNestedContext()
    {
        Context inner = makeTemplateContext("harmless");
        Context outer = makeTemplateContext("something <nested>").add("nested", inner);
        assertRenderingResult("something harmless", outer);
    }

    @Test
    public void testNestedTemplate()
    {
        Template inner = loader.getTemplate()
            .fromString("harmless")
            .build();

        Context outer = makeTemplateContext("something <nested>");

        assertThrowsIllegalArgumentException(() -> outer.add("nested", inner));
    }
}
