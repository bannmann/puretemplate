package com.github.bannmann.puretemplate.test;

import org.junit.Test;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.STGroupFile;

import static org.junit.Assert.assertEquals;

public class TestLists extends BaseTest {
    @Test public void testJustCat() throws Exception {
        ST e = new ST(
                "<[names,phones]>"
            );
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        String expecting = "TerTom12";
        assertEquals(expecting, e.render());
    }

    @Test public void testListLiteralWithEmptyElements() throws Exception {
        ST e = new ST(
            "<[\"Ter\",,\"Jesse\"]:{n | <i>:<n>}; separator=\", \", null={foo}>"
        );
        String expecting = "1:Ter, foo, 2:Jesse";
        assertEquals(expecting, e.render());
    }

    @Test public void testListLiteralWithEmptyFirstElement() throws Exception {
        ST e = new ST(
            "<[,\"Ter\",\"Jesse\"]:{n | <i>:<n>}; separator=\", \", null={foo}>"
        );
        String expecting = "foo, 1:Ter, 2:Jesse";
        assertEquals(expecting, e.render());
    }

    @Test public void testLength() throws Exception {
        ST e = new ST(
                "<length([names,phones])>"
            );
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        String expecting = "4";
        assertEquals(expecting, e.render());
    }

    @Test public void testCat2Attributes() throws Exception {
        ST e = new ST(
                "<[names,phones]; separator=\", \">"
            );
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        String expecting = "Ter, Tom, 1, 2";
        assertEquals(expecting, e.render());
    }

    @Test public void testCat2AttributesWithApply() throws Exception {
        ST e = new ST(
                "<[names,phones]:{a|<a>.}>"
            );
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        String expecting = "Ter.Tom.1.2.";
        assertEquals(expecting, e.render());
    }

    @Test public void testCat3Attributes() throws Exception {
        ST e = new ST(
                "<[names,phones,salaries]; separator=\", \">"
            );
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        e.add("salaries", "big");
        e.add("salaries", "huge");
        String expecting = "Ter, Tom, 1, 2, big, huge";
        assertEquals(expecting, e.render());
    }

    @Test public void testCatWithTemplateApplicationAsElement() throws Exception {
        ST e = new ST(
                "<[names:{n|<n>!},phones]; separator=\", \">"
            );
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones" , "1");
        e.add("phones", "2");
        String expecting = "Ter!, Tom!, 1, 2";
        assertEquals(expecting, e.render());
    }

    @Test public void testCatWithIFAsElement() throws Exception {
        ST e = new ST(
                "<[{<if(names)>doh<endif>},phones]; separator=\", \">"
            );
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones" , "1");
        e.add("phones", "2");
        String expecting = "doh, 1, 2";
        assertEquals(expecting, e.render());
    }

    @Test public void testCatNullValues() throws Exception {
        // [a, b] must behave like <a><b>; if a==b==null, blank output
        // unless null argument.
        ST e = new ST(
                "<[no,go]; null=\"foo\", separator=\", \">"
            );
        e.add("phones", "1");
        e.add("phones", "2");
        String expecting = "foo, foo";
        assertEquals(expecting, e.render());
    }

    @Test public void testCatWithNullTemplateApplicationAsElement() throws Exception {
        ST e = new ST(
                "<[names:{n|<n>!},\"foo\"]:{a|x}; separator=\", \">"
            );
        e.add("phones", "1");
        e.add("phones", "2");
        String expecting = "x";  // only one since template application gives nothing
        assertEquals(expecting, e.render());
    }

    @Test public void testCatWithNestedTemplateApplicationAsElement() throws Exception {
        ST e = new ST(
                "<[names, [\"foo\",\"bar\"]:{x | <x>!},phones]; separator=\", \">"
            );
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        String expecting = "Ter, Tom, foo!, bar!, 1, 2";
        assertEquals(expecting, e.render());
    }

    @Test public void testListAsTemplateArgument() throws Exception {
        String templates =
                "test(names,phones) ::= \"<foo([names,phones])>\""+newline+
                "foo(items) ::= \"<items:{a | *<a>*}>\""+newline
                ;
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");
        ST e = group.getInstanceOf("test");
        e.add("names", "Ter");
        e.add("names", "Tom");
        e.add("phones", "1");
        e.add("phones", "2");
        String expecting = "*Ter**Tom**1**2*";
        String result = e.render();
        assertEquals(expecting, result);
    }

    @Test public void testListWithTwoEmptyListsCollapsesToEmptyList() throws Exception {
        ST e = new ST(
            "<[[],[]]:{x | <x>!}; separator=\", \">"
        );
        e.add("names", "Ter");
        e.add("names", "Tom");
        String expecting = "";
        assertEquals(expecting, e.render());
    }

}
