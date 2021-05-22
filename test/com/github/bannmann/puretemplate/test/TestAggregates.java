package com.github.bannmann.puretemplate.test;

import org.junit.Test;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.STGroupString;

import static org.junit.Assert.assertEquals;

/** */
public class TestAggregates extends BaseTest {
    @Test public void testApplyAnonymousTemplateToAggregateAttribute() throws Exception {
        ST st =
            new ST("<items:{it|<it.id>: <it.lastName>, <it.firstName>\n}>");
        // also testing wacky spaces in aggregate spec
        st.addAggr("items.{ firstName ,lastName, id }", "Ter", "Parr", 99);
        st.addAggr("items.{firstName, lastName ,id}", "Tom", "Burns", 34);
        String expecting =
            "99: Parr, Ter"+newline +
            "34: Burns, Tom"+newline;
        assertEquals(expecting, st.render());
    }

    public static class Decl {
        String name;
        String type;
        public Decl(String name, String type) {this.name=name; this.type=type;}
        public String getName() {return name;}
        public String getType() {return type;}
    }

    @Test public void testComplicatedIndirectTemplateApplication() throws Exception {
        String templates =
            "group Java;"+newline +
            ""+newline +
            "file(variables) ::= <<\n" +
            "<variables:{ v | <v.decl:(v.format)()>}; separator=\"\\n\">"+newline +
            ">>"+newline+
            "intdecl(decl) ::= \"int <decl.name> = 0;\""+newline +
            "intarray(decl) ::= \"int[] <decl.name> = null;\""+newline
            ;
        STGroup group = new STGroupString(templates);
        ST f = group.getInstanceOf("file");
        f.addAggr("variables.{ decl,format }", new Decl("i", "int"), "intdecl");
        f.addAggr("variables.{decl ,  format}", new Decl("a", "int-array"), "intarray");
        //System.out.println("f='"+f+"'");
        String expecting = "int i = 0;" +newline+
                           "int[] a = null;";
        assertEquals(expecting, f.render());
    }

}
