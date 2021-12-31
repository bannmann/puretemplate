package org.puretemplate.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.Synchronized;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.puretemplate.BaseTest;

class TestDateRenderers extends BaseTest
{
    private static final LocalDateTime LOCAL_2005_07_05 = LocalDateTime.of(2005, Month.JULY, 5, 0, 0);
    private static final LocalDateTime LOCAL_2012_06_12 = LocalDateTime.of(2012, Month.JUNE, 12, 0, 0);
    private static final ZoneId LOS_ANGELES = ZoneId.of("America/Los_Angeles");
    private static final Locale PORTUGUESE = new Locale("pt");

    private static Date toDate(LocalDateTime localDateTime)
    {
        return toCalendar(localDateTime).getTime();
    }

    private static GregorianCalendar toCalendar(LocalDateTime localDateTime)
    {
        return GregorianCalendar.from(toZonedDateTime(localDateTime));
    }

    private static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime)
    {
        return localDateTime.atZone(ZoneId.systemDefault());
    }

    @SuppressWarnings("deprecation")
    private ObjectTypedDateRenderer objectTypedDateRenderer;

    private DateTimeRenderer<TemporalAccessor> dateTimeRenderer;
    private DateRenderer dateRenderer;
    private CalendarRenderer calendarRenderer;

    @BeforeEach
    @Override
    @SuppressWarnings("deprecation")
    public void setUp()
    {
        super.setUp();
        objectTypedDateRenderer = new ObjectTypedDateRenderer();
        dateTimeRenderer = new DateTimeRenderer<>();
        dateRenderer = new DateRenderer();
        calendarRenderer = new CalendarRenderer();
    }

    static Arguments[] localFormats()
    {
        return new Arguments[]{
            args("7/5/05, 12:00 AM", null, LOCAL_2005_07_05, Locale.US),
            args("2005.07.05", "yyyy.MM.dd", LOCAL_2005_07_05, Locale.ROOT),
            args("7/5/05, 12:00 AM", "short", LOCAL_2005_07_05, Locale.US),
            args("Jul 5, 2005", "date:medium", LOCAL_2005_07_05, Locale.US),
            args("12:00:00 AM", "time:medium", LOCAL_2005_07_05, Locale.US),
            args("12 de junho de 2012", "dd 'de' MMMM 'de' yyyy", LOCAL_2012_06_12, PORTUGUESE)
        };
    }

    @ParameterizedTest(name = "[{index}] ''{0}''")
    @MethodSource("localFormats")
    void testLocalFormats(String expected, String formatString, LocalDateTime localDateTime, Locale locale)
    {
        assertResult(toCalendar(localDateTime), objectTypedDateRenderer, expected, formatString, locale);
        assertResult(toCalendar(localDateTime), calendarRenderer, expected, formatString, locale);
        assertResult(toDate(localDateTime), objectTypedDateRenderer, expected, formatString, locale);
        assertResult(toDate(localDateTime), dateRenderer, expected, formatString, locale);
        assertResult(toZonedDateTime(localDateTime), dateTimeRenderer, expected, formatString, locale);
        assertResult(localDateTime, dateTimeRenderer, expected, formatString, locale);
    }

    private <T> void assertResult(
        T input, AttributeRenderer<T> renderer, String expected, String formatString, Locale locale)
    {
        assertThat(renderer.render(input, formatString, locale)).isEqualTo(expected);
    }

    static Arguments[] defaultStrings()
    {
        return new Arguments[]{
            args("12:00 AM", LocalTime.MIDNIGHT, Locale.US),
            args("7/5/05", LOCAL_2005_07_05.toLocalDate(), Locale.US),
            args("7/5/05, 12:00 AM", LOCAL_2005_07_05, Locale.US)
        };
    }

    @ParameterizedTest(name = "[{index}] ''{0}''")
    @MethodSource("defaultStrings")
    void testDefaults(String expected, TemporalAccessor temporalAccessor, Locale locale)
    {
        assertThat(dateTimeRenderer.render(temporalAccessor, null, locale)).isEqualTo(expected);
    }

    @SuppressWarnings("deprecation")
    static Arguments[] zoneRenderers()
    {
        return new Arguments[]{
            zoneRendererArgs("ObjectTypedCalendar", new ObjectTypedDateRenderer(), TestDateRenderers::toCalendar),
            zoneRendererArgs("ObjectTypedDate", new ObjectTypedDateRenderer(), TestDateRenderers::toDate),
            zoneRendererArgs("Calendar", new CalendarRenderer(), TestDateRenderers::toCalendar),
            zoneRendererArgs("Date", new DateRenderer(), TestDateRenderers::toDate),
            zoneRendererArgs("ZonedDateTime", new DateTimeRenderer<>(), local -> local.atZone(LOS_ANGELES))
        };
    }

    private static <T> Arguments zoneRendererArgs(
        String testName, AttributeRenderer<T> renderer, Function<LocalDateTime, T> converter)
    {
        return args(testName, renderer, converter);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("zoneRenderers")
    <T> void testZonedFormat(
        @SuppressWarnings("unused") String testName,
        AttributeRenderer<T> renderer,
        Function<LocalDateTime, T> converter)
    {
        String actual = callWithChangedZone(TimeZone.getTimeZone(LOS_ANGELES), () -> {
            T value = converter.apply(LOCAL_2005_07_05);
            return renderer.render(value, "full", Locale.US);
        });

        assertThat(actual).isEqualTo("Tuesday, July 5, 2005 at 12:00:00 AM Pacific Daylight Time");
    }

    @Synchronized
    private String callWithChangedZone(TimeZone timeZone, Supplier<String> supplier)
    {
        TimeZone originalTimeZone = TimeZone.getDefault();
        try
        {
            TimeZone.setDefault(timeZone);
            return supplier.get();
        }
        finally
        {
            TimeZone.setDefault(originalTimeZone);
        }
    }
}
