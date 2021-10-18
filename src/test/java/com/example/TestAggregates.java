package com.example;

import org.junit.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.model.Aggregate;

public class TestAggregates extends BaseTest
{
    @Test
    public void testApplyAnonymousTemplateToAggregateAttribute()
    {
        Context context = makeTemplateContext("<items:{it|<it.id>: <it.lastName>, <it.firstName>\n}>");

        context.add("items",
            Aggregate.build()
                .properties("firstName", "lastName", "id")
                .withValues("Ter", "Parr", 99));

        context.add("items",
            Aggregate.build()
                .properties("firstName", "lastName", "id")
                .withValues("Tom", "Burns", 34));

        assertRenderingResult("99: Parr, Ter" + NEWLINE + "34: Burns, Tom" + NEWLINE, context);
    }

    public static class Decl
    {
        String name;
        String type;

        public Decl(String name, String type)
        {
            this.name = name;
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }
    }

    @Test
    public void testComplicatedIndirectTemplateApplication()
    {
        String templates = "group Java;" +
            NEWLINE +
            "" +
            NEWLINE +
            "file(variables) ::= <<\n" +
            "<variables:{ v | <v.decl:(v.format)()>}; separator=\"\\n\">" +
            NEWLINE +
            ">>" +
            NEWLINE +
            "intdecl(decl) ::= \"int <decl.name> = 0;\"" +
            NEWLINE +
            "intarray(decl) ::= \"int[] <decl.name> = null;\"" +
            NEWLINE;

        Context context = loadGroupFromString(templates).getTemplate("file")
            .createContext()
            .add("variables",
                Aggregate.build()
                    .properties("decl", "format")
                    .withValues(new Decl("i", "int"), "intdecl"))
            .add("variables",
                Aggregate.build()
                    .properties("decl", "format")
                    .withValues(new Decl("a", "int-array"), "intarray"));

        assertRenderingResult("int i = 0;" + NEWLINE + "int[] a = null;", context);
    }
}
