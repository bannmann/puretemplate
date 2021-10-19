package com.example;

import static org.junit.Assert.assertEquals;

import java.util.TreeMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.junit.jupiter.api.Test;
import org.puretemplate.BaseTest;
import org.puretemplate.Context;
import org.puretemplate.error.RuntimeMessage;
import org.puretemplate.exception.NoSuchPropertyException;
import org.puretemplate.misc.ErrorBufferAllErrors;
import org.puretemplate.model.ModelAdaptor;

class TestModelAdaptors extends BaseTest
{
    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    protected static class UserAdaptor implements ModelAdaptor<User>
    {
        @Override
        public Object getProperty(User model, Object property, String propertyName) throws NoSuchPropertyException
        {
            switch (propertyName)
            {
                case "id":
                    return model.id;
                case "name":
                    return model.getName();
                default:
                    throw new NoSuchPropertyException(null, model, "User." + propertyName);
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    protected static class UserAdaptorConst implements ModelAdaptor<User>
    {
        @Override
        public Object getProperty(User model, Object property, String propertyName) throws NoSuchPropertyException
        {
            switch (propertyName)
            {
                case "id":
                    return "const id value";
                case "name":
                    return "const name value";
                default:
                    throw new NoSuchPropertyException(null, model, "User." + propertyName);
            }
        }
    }

    protected static class SuperUser extends User
    {
        int bitmask;

        public SuperUser(int id, String name)
        {
            super(id, name);
            bitmask = 0x8080;
        }

        @Override
        public String getName()
        {
            return "super " + super.getName();
        }
    }

    @Test
    void testSimpleAdaptor()
    {
        String templates = "foo(x) ::= \"<x.id>: <x.name>\"\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .registerModelAdaptor(User.class, new UserAdaptor())
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", new User(100, "parrt"));

        assertRenderingResult("100: parrt", context);
    }

    @Test
    void testAdaptorAndBadProp()
    {
        String templates = "foo(x) ::= \"<x.qqq>\"\n";
        ErrorBufferAllErrors errors = new ErrorBufferAllErrors();

        Context context = loader.getGroup()
            .fromString(templates)
            .withErrorListener(errors)
            .registerModelAdaptor(User.class, new UserAdaptor())
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", new User(100, "parrt"));

        assertRenderingResult("", context);

        RuntimeMessage msg = (RuntimeMessage) errors.getErrors()
            .get(0);
        NoSuchPropertyException e = (NoSuchPropertyException) msg.getCause();
        assertEquals("User.qqq", e.getPropertyName());
    }

    @Test
    void testAdaptorCoversSubclass()
    {
        String templates = "foo(x) ::= \"<x.id>: <x.name>\"\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .registerModelAdaptor(User.class, new UserAdaptor())
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", new SuperUser(100, "parrt")); // create subclass of User

        assertRenderingResult("100: super parrt", context);
    }

    @Test
    void testSeesMostSpecificAdaptor()
    {
        String templates = "foo(x) ::= \"<x.id>: <x.name>\"\n";

        Context context = loader.getGroup()
            .fromString(templates)
            .registerModelAdaptor(User.class, new UserAdaptor())
            .registerModelAdaptor(SuperUser.class, new UserAdaptorConst()) // most specific
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", new User(100, "parrt"));
        assertRenderingResult("100: parrt", context);

        context.remove("x")
            .add("x", new SuperUser(100, "parrt"));
        assertRenderingResult("const id value: const name value", context); // sees UserAdaptorConst
    }

    /**
     * @see <a href="https://github.com/antlr/stringtemplate4/issues/214">antlr/stringtemplate4#214</a>
     */
    @Test
    void testHandlesNullKeys()
    {
        String templates = "foo(x, y) ::= \"<x.(y); null={NULL}>\"";

        Context context = loader.getGroup()
            .fromString(templates)
            .build()
            .getTemplate("foo")
            .createContext()
            .add("x", new TreeMap<String, String>())
            .add("y", null);

        assertRenderingResult("NULL", context);
    }

    /**
     * @see <a href="https://github.com/antlr/stringtemplate4/issues/214">antlr/stringtemplate4#214</a>
     */
    @Test
    void testHandlesKeysNotComparableToString()
    {
        String templates = "foo(x) ::= \"<x.keys>\"";

        Context context = loader.getGroup()
            .fromString(templates)
            .build()
            .getTemplate("foo")
            .createContext();

        TreeMap<Integer, String> x = new TreeMap<>();
        x.put(1, "value");
        context.add("x", x);

        assertRenderingResult("1", context);
    }
}
