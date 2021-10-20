package com.example;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.Template;

class TestAttributes extends BaseTest
{
    @Test
    void testNestedContext()
    {
        Context inner = makeTemplateContext("harmless");
        Context outer = makeTemplateContext("something <nested>").add("nested", inner);
        assertRenderingResult("something harmless", outer);
    }

    @Test
    void testNestedTemplate()
    {
        Template inner = loader.getTemplate()
            .fromString("harmless")
            .build();

        Context outer = makeTemplateContext("something <nested>");

        assertThrowsIllegalArgumentException(() -> outer.add("nested", inner));
    }
}
