package org.puretemplate.model;

import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.puretemplate.BaseTest;

class TestStringRenderer extends BaseTest
{
    private StringRenderer stringRenderer;

    @Override
    @BeforeEach
    public void setUp()
    {
        super.setUp();

        stringRenderer = new StringRenderer();
    }

    static Arguments[] formatStrings()
    {
        return new Arguments[]{
            args("upper", "hi there", "HI THERE"),
            args("upper", "hI tHERE", "HI THERE"),
            args("lower", "HI THERE", "hi there"),
            args("lower", "Hi There", "hi there"),
            args("cap", "hi there", "Hi there"),
            args("url-encode", "a b", "a+b"),
            args("xml-encode", "a<b> &\t\b", "a&lt;b&gt; &amp;\t&#8;"),
            args("xml-encode", "\uD83E\uDE73", "&#129651;"),
            args("%6s", "hi", "    hi")
        };
    }

    @ParameterizedTest(name = "[{index}] {0} ''{1}''")
    @MethodSource("formatStrings")
    void testFormatStrings(String formatString, String inputValue, String expectedOutput)
    {
        String actual = stringRenderer.render(inputValue, formatString, Locale.ROOT);
        Assertions.assertThat(actual)
            .isEqualTo(expectedOutput);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = { "cap", "url-encode", "xml-encode" })
    void testFormatStringWithEmptyInput(String formatString)
    {
        String actual = stringRenderer.render("", formatString, Locale.ROOT);
        Assertions.assertThat(actual)
            .isEmpty();
    }
}
