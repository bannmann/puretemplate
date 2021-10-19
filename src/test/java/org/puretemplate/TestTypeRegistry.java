package org.puretemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @see <a href="https://github.com/antlr/stringtemplate4/issues/122">antlr/stringtemplate4#122</a>
 */
class TestTypeRegistry
{
    static class A
    {
    }

    static class B extends A
    {
    }

    @Test
    void registryWithObject()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(Object.class, "Object");
        Assertions.assertEquals("Object", registry.get(Object.class));
        assertEquals("Object", registry.get(A.class));
        assertEquals("Object", registry.get(B.class));
    }

    @Test
    void registryWithA()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(A.class, "A");
        assertNull(registry.get(Object.class));
        assertEquals("A", registry.get(A.class));
        assertEquals("A", registry.get(B.class));
    }

    @Test
    void registryWithB()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(B.class, "B");
        assertNull(registry.get(Object.class));
        assertNull(registry.get(A.class));
        assertEquals("B", registry.get(B.class));
    }

    @Test
    void registryWithObjectAndA()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(Object.class, "Object");
        registry.put(A.class, "A");
        assertEquals("Object", registry.get(Object.class));
        assertEquals("A", registry.get(A.class));
        assertEquals("A", registry.get(B.class));
    }

    @Test
    void registryWithObjectAndB()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(Object.class, "Object");
        registry.put(B.class, "B");
        assertEquals("Object", registry.get(Object.class));
        assertEquals("Object", registry.get(A.class));
        assertEquals("B", registry.get(B.class));
    }

    @Test
    void registryWithAAndB()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(A.class, "A");
        registry.put(B.class, "B");
        assertNull(registry.get(Object.class));
        assertEquals("A", registry.get(A.class));
        assertEquals("B", registry.get(B.class));
    }

    @Test
    void registryWithObjectAndAAndB()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(Object.class, "Object");
        registry.put(A.class, "A");
        registry.put(B.class, "B");
        assertEquals("Object", registry.get(Object.class));
        assertEquals("A", registry.get(A.class));
        assertEquals("B", registry.get(B.class));
    }
}
