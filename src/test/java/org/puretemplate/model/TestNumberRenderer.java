package org.puretemplate.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.puretemplate.BaseTest;

class TestNumberRenderer extends BaseTest
{
    private static final Locale POLISH = new Locale("pl");

    private NumberRenderer numberRenderer;

    @Override
    @BeforeEach
    public void setUp()
    {
        super.setUp();
        numberRenderer = new NumberRenderer();
    }

    static Arguments[] bulkTestData()
    {
        return new Arguments[]{
            args("-2100", "%d", -2100, Locale.ROOT),
            args("3.142", "%2.3f", 3.14159, Locale.ROOT),
            args("-2\u00A0100", "%,d", -2100, POLISH),
            args("3,142", "%2.3f", 3.14159, POLISH)
        };
    }

    @ParameterizedTest(name = "[{index}] ''{0}''")
    @MethodSource("bulkTestData")
    void testBulk(String expected, String formatString, Number value, Locale locale)
    {
        assertThat(numberRenderer.render(value, formatString, locale)).isEqualTo(expected);
    }
}
