package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class TestNullAndEmptyValues extends BaseTest
{
    public static class T
    {
        String template;
        Object x;
        String expecting;

        String result;

        public T(String template, Object x, String expecting)
        {
            this.template = template;
            this.x = x;
            this.expecting = expecting;
        }

        public T(T t)
        {
            this.template = t.template;
            this.x = t.x;
            this.expecting = t.expecting;
        }

        @Override
        public String toString()
        {
            String s = x.toString();
            if (x.getClass()
                .isArray())
            {
                s = Arrays.toString((Object[]) x);
            }
            return "('" + template + "', " + s + ", '" + expecting + "', '" + result + "')";
        }
    }

    final static Object UNDEF = "<undefined>";
    final static List<?> LIST0 = List.of();

    final static T[] singleValuedTests = new T[]{
        new T("<x>", UNDEF, ""),
        new T("<x>", null, ""),
        new T("<x>", "", ""),
        new T("<x>", LIST0, ""),

        new T("<x:t()>", UNDEF, ""),
        new T("<x:t()>", null, ""),
        new T("<x:t()>", "", ""),
        new T("<x:t()>", LIST0, ""),

        new T("<x; null={y}>", UNDEF, "y"),
        new T("<x; null={y}>", null, "y"),
        new T("<x; null={y}>", "", ""),
        new T("<x; null={y}>", LIST0, ""),

        new T("<x:t(); null={y}>", UNDEF, "y"),
        new T("<x:t(); null={y}>", null, "y"),
        new T("<x:t(); null={y}>", "", ""),
        new T("<x:t(); null={y}>", LIST0, ""),

        new T("<if(x)>y<endif>", UNDEF, ""),
        new T("<if(x)>y<endif>", null, ""),
        new T("<if(x)>y<endif>", "", "y"),
        new T("<if(x)>y<endif>", LIST0, ""),

        new T("<if(x)>y<else>z<endif>", UNDEF, "z"),
        new T("<if(x)>y<else>z<endif>", null, "z"),
        new T("<if(x)>y<else>z<endif>", "", "y"),
        new T("<if(x)>y<else>z<endif>", LIST0, "z"),
        };

    final static String[] LISTa = { "a" };
    final static String[] LISTab = { "a", "b" };
    final static String[] LISTnull = { null };
    final static String[] LISTa_null = { "a", null };
    final static String[] LISTnull_b = { null, "b" };
    final static String[] LISTa_null_b = { "a", null, "b" };

    final static T[] multiValuedTests = new T[]{
        new T("<x>", LIST0, ""),
        new T("<x>", LISTa, "a"),
        new T("<x>", LISTab, "ab"),
        new T("<x>", LISTnull, ""),
        new T("<x>", LISTnull_b, "b"),
        new T("<x>", LISTa_null, "a"),
        new T("<x>", LISTa_null_b, "ab"),

        new T("<x; null={y}>", LIST0, ""),
        new T("<x; null={y}>", LISTa, "a"),
        new T("<x; null={y}>", LISTab, "ab"),
        new T("<x; null={y}>", LISTnull, "y"),
        new T("<x; null={y}>", LISTnull_b, "yb"),
        new T("<x; null={y}>", LISTa_null, "ay"),
        new T("<x; null={y}>", LISTa_null_b, "ayb"),

        new T("<x; separator={,}>", LIST0, ""),
        new T("<x; separator={,}>", LISTa, "a"),
        new T("<x; separator={,}>", LISTab, "a,b"),
        new T("<x; separator={,}>", LISTnull, ""),
        new T("<x; separator={,}>", LISTnull_b, "b"),
        new T("<x; separator={,}>", LISTa_null, "a"),
        new T("<x; separator={,}>", LISTa_null_b, "a,b"),

        new T("<x; null={y}, separator={,}>", LIST0, ""),
        new T("<x; null={y}, separator={,}>", LISTa, "a"),
        new T("<x; null={y}, separator={,}>", LISTab, "a,b"),
        new T("<x; null={y}, separator={,}>", LISTnull, "y"),
        new T("<x; null={y}, separator={,}>", LISTnull_b, "y,b"),
        new T("<x; null={y}, separator={,}>", LISTa_null, "a,y"),
        new T("<x; null={y}, separator={,}>", LISTa_null_b, "a,y,b"),

        new T("<if(x)>y<endif>", LIST0, ""),
        new T("<if(x)>y<endif>", LISTa, "y"),
        new T("<if(x)>y<endif>", LISTab, "y"),
        new T("<if(x)>y<endif>", LISTnull, "y"),
        new T("<if(x)>y<endif>", LISTnull_b, "y"),
        new T("<if(x)>y<endif>", LISTa_null, "y"),
        new T("<if(x)>y<endif>", LISTa_null_b, "y"),

        new T("<x:{it | <it>}>", LIST0, ""),
        new T("<x:{it | <it>}>", LISTa, "a"),
        new T("<x:{it | <it>}>", LISTab, "ab"),
        new T("<x:{it | <it>}>", LISTnull, ""),
        new T("<x:{it | <it>}>", LISTnull_b, "b"),
        new T("<x:{it | <it>}>", LISTa_null, "a"),
        new T("<x:{it | <it>}>", LISTa_null_b, "ab"),

        new T("<x:{it | <it>}; null={y}>", LIST0, ""),
        new T("<x:{it | <it>}; null={y}>", LISTa, "a"),
        new T("<x:{it | <it>}; null={y}>", LISTab, "ab"),
        new T("<x:{it | <it>}; null={y}>", LISTnull, "y"),
        new T("<x:{it | <it>}; null={y}>", LISTnull_b, "yb"),
        new T("<x:{it | <it>}; null={y}>", LISTa_null, "ay"),
        new T("<x:{it | <it>}; null={y}>", LISTa_null_b, "ayb"),

        new T("<x:{it | <i>.<it>}>", LIST0, ""),
        new T("<x:{it | <i>.<it>}>", LISTa, "1.a"),
        new T("<x:{it | <i>.<it>}>", LISTab, "1.a2.b"),
        new T("<x:{it | <i>.<it>}>", LISTnull, ""),
        new T("<x:{it | <i>.<it>}>", LISTnull_b, "1.b"),
        new T("<x:{it | <i>.<it>}>", LISTa_null, "1.a"),
        new T("<x:{it | <i>.<it>}>", LISTa_null_b, "1.a2.b"),

        new T("<x:{it | <i>.<it>}; null={y}>", LIST0, ""),
        new T("<x:{it | <i>.<it>}; null={y}>", LISTa, "1.a"),
        new T("<x:{it | <i>.<it>}; null={y}>", LISTab, "1.a2.b"),
        new T("<x:{it | <i>.<it>}; null={y}>", LISTnull, "y"),
        new T("<x:{it | <i>.<it>}; null={y}>", LISTnull_b, "y1.b"),
        new T("<x:{it | <i>.<it>}; null={y}>", LISTa_null, "1.ay"),
        new T("<x:{it | <i>.<it>}; null={y}>", LISTa_null_b, "1.ay2.b"),

        new T("<x:{it | x<if(!it)>y<endif>}; null={z}>", LIST0, ""),
        new T("<x:{it | x<if(!it)>y<endif>}; null={z}>", LISTa, "x"),
        new T("<x:{it | x<if(!it)>y<endif>}; null={z}>", LISTab, "xx"),
        new T("<x:{it | x<if(!it)>y<endif>}; null={z}>", LISTnull, "z"),
        new T("<x:{it | x<if(!it)>y<endif>}; null={z}>", LISTnull_b, "zx"),
        new T("<x:{it | x<if(!it)>y<endif>}; null={z}>", LISTa_null, "xz"),
        new T("<x:{it | x<if(!it)>y<endif>}; null={z}>", LISTa_null_b, "xzx"),

        new T("<x:t():u(); null={y}>", LIST0, ""),
        new T("<x:t():u(); null={y}>", LISTa, "a"),
        new T("<x:t():u(); null={y}>", LISTab, "ab"),
        new T("<x:t():u(); null={y}>", LISTnull, "y"),
        new T("<x:t():u(); null={y}>", LISTnull_b, "yb"),
        new T("<x:t():u(); null={y}>", LISTa_null, "ay"),
        new T("<x:t():u(); null={y}>", LISTa_null_b, "ayb")
    };

    final static T[] listTests = new T[]{
        new T("<[]>", UNDEF, ""),
        new T("<[]; null={x}>", UNDEF, ""),
        new T("<[]:{it | x}>", UNDEF, ""),
        new T("<[[],[]]:{it| x}>", UNDEF, ""),
        new T("<[]:t()>", UNDEF, ""),
        };

    @Test
    void testSingleValued()
    {
        List<T> failed = testMatrix(singleValuedTests);
        List<T> expecting = Collections.emptyList();
        assertArrayEquals(expecting.toArray(), failed.toArray(), "failed tests " + failed);
    }

    @Test
    void testMultiValued()
    {
        List<T> failed = testMatrix(multiValuedTests);
        List<T> expecting = Collections.emptyList();
        assertArrayEquals(expecting.toArray(), failed.toArray(), "failed tests " + failed);
    }

    @Test
    void testLists()
    {
        List<T> failed = testMatrix(listTests);
        List<T> expecting = Collections.emptyList();
        assertArrayEquals(expecting.toArray(), failed.toArray(), "failed tests " + failed);
    }

    public List<T> testMatrix(T[] tests)
    {
        List<T> failed = new ArrayList<T>();
        for (T t : tests)
        {
            T test = new T(t); // dup since we might mod with result
            STGroup group = new LegacyBareStGroup();
            group.defineTemplate("t", "x", "<x>");
            group.defineTemplate("u", "x", "<x>");
            group.defineTemplate("test", "x", test.template);
            ST st = group.getInstanceOf("test");
            if (test.x != UNDEF)
            {
                st.add("x", test.x);
            }
            String result = st.render();
            if (!result.equals(test.expecting))
            {
                test.result = result;
                failed.add(test);
            }
        }
        return failed;
    }

    @Test
    void testSeparatorWithNullFirstValue() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.add("name", null); // null is added to list, but ignored in iteration
        st.add("name", "Tom");
        st.add("name", "Sumana");
        assertRenderingResult("hi Tom, Sumana!", st);
    }

    @Test
    void testTemplateAppliedToNullIsEmpty() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name:t()>");
        group.defineTemplate("t", "x", "<x>");
        ST st = group.getInstanceOf("test");
        st.add("name", null);
        assertRenderingResult("", st);
    }

    @Test
    void testTemplateAppliedToMissingValueIsEmpty() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name:t()>");
        group.defineTemplate("t", "x", "<x>");
        ST st = group.getInstanceOf("test");
        assertRenderingResult("", st);
    }

    @Test
    void testSeparatorWithNull2ndValue() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", null);
        st.add("name", "Sumana");
        assertRenderingResult("hi Ter, Sumana!", st);
    }

    @Test
    void testSeparatorWithNullLastValue() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", null);
        assertRenderingResult("hi Ter, Tom!", st);
    }

    @Test
    void testSeparatorWithTwoNullValuesInRow() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; separator=\", \">!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", null);
        st.add("name", null);
        st.add("name", "Sri");
        assertRenderingResult("hi Ter, Tom, Sri!", st);
    }

    @Test
    void testTwoNullValues() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "hi <name; null=\"x\">!");
        ST st = group.getInstanceOf("test");
        st.add("name", null);
        st.add("name", null);
        assertRenderingResult("hi xx!", st);
    }

    @Test
    void testNullListItemNotCountedForIteratorIndex() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "name", "<name:{n | <i>:<n>}>");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", null);
        st.add("name", null);
        st.add("name", "Jesse");
        assertRenderingResult("1:Ter2:Jesse", st);
    }

    @Test
    void testSizeZeroButNonNullListGetsNoOutput() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "users", "begin\n" + "<users>\n" + "end\n");
        ST t = group.getInstanceOf("test");
        t.add("users", null);
        assertRenderingResult("begin" + NEWLINE + "end", t);
    }

    @Test
    void testNullListGetsNoOutput() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "users", "begin\n" + "<users:{u | name: <u>}; separator=\", \">\n" + "end\n");
        ST t = group.getInstanceOf("test");
        assertRenderingResult("begin" + NEWLINE + "end", t);
    }

    @Test
    void testEmptyListGetsNoOutput() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "users", "begin\n" + "<users:{u | name: <u>}; separator=\", \">\n" + "end\n");
        ST t = group.getInstanceOf("test");
        t.add("users", List.of());
        assertRenderingResult("begin" + NEWLINE + "end", t);
    }

    @Test
    void testMissingDictionaryValue() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "m", "<m.foo>");
        ST t = group.getInstanceOf("test");
        t.add("m", Collections.emptyMap());
        assertRenderingResult("", t);
    }

    @Test
    void testMissingDictionaryValue2() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "m", "<if(m.foo)>[<m.foo>]<endif>");
        ST t = group.getInstanceOf("test");
        t.add("m", Collections.emptyMap());
        assertRenderingResult("", t);
    }

    @Test
    void testMissingDictionaryValue3() throws IOException
    {
        STGroup group = new LegacyBareStGroup();
        group.defineTemplate("test", "m", "<if(m.foo)>[<m.foo>]<endif>");
        ST t = group.getInstanceOf("test");
        t.add("m", Collections.singletonMap("foo", null));
        assertRenderingResult("", t);
    }

    @Test
    void TestSeparatorEmittedForEmptyIteratorValue()
    {
        Context context = makeTemplateContext("<values:{v|<if(v)>x<endif>}; separator=\" \">").add("values",
            new boolean[]{ true, false, true });
        assertRenderingResult("x  x", context);
    }

    @Test
    void TestSeparatorEmittedForEmptyIteratorValu3333e() throws IOException
    {
        String dir = getRandomDir();
        String groupFile = "filter ::= [\"b\":, default: key]\n" +
            "t() ::= <%<[\"a\", \"b\", \"c\", \"b\"]:{it | <filter.(it)>}; separator=\",\">%>\n";
        writeFile(dir, "group.stg", groupFile);
        STGroup group = STGroupFilePath.createWithDefaults(dir + "/group.stg");

        ST st = group.getInstanceOf("t");
        assertRenderingResult("a,,c,", st);
    }

    @Test
    void TestSeparatorEmittedForEmptyIteratorValue2()
    {
        Context context = makeTemplateContext("<values; separator=\" \">").add("values", new String[]{ "x", "", "y" });
        assertRenderingResult("x  y", context);
    }
}
