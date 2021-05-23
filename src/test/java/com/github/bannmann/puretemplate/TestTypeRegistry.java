package com.github.bannmann.puretemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.bannmann.puretemplate.misc.TypeRegistry;

/**
 * @see <a href="https://github.com/antlr/stringtemplate4/issues/122">antlr/stringtemplate4#122</a>
 */
public class TestTypeRegistry
{
    static class A
    {
    }

    static class B extends A
    {
    }

    @Test
    public void registryWithObject()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(Object.class, "Object");
        assertEquals("Object", registry.get(Object.class));
        assertEquals("Object", registry.get(A.class));
        assertEquals("Object", registry.get(B.class));
    }

    @Test
    public void registryWithA()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(A.class, "A");
        assertNull(registry.get(Object.class));
        assertEquals("A", registry.get(A.class));
        assertEquals("A", registry.get(B.class));
    }

    @Test
    public void registryWithB()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(B.class, "B");
        assertNull(registry.get(Object.class));
        assertNull(registry.get(A.class));
        assertEquals("B", registry.get(B.class));
    }

    @Test
    public void registryWithObjectAndA()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(Object.class, "Object");
        registry.put(A.class, "A");
        assertEquals("Object", registry.get(Object.class));
        assertEquals("A", registry.get(A.class));
        assertEquals("A", registry.get(B.class));
    }

    @Test
    public void registryWithObjectAndB()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(Object.class, "Object");
        registry.put(B.class, "B");
        assertEquals("Object", registry.get(Object.class));
        assertEquals("Object", registry.get(A.class));
        assertEquals("B", registry.get(B.class));
    }

    @Test
    public void registryWithAAndB()
    {
        TypeRegistry<String> registry = new TypeRegistry<String>();
        registry.put(A.class, "A");
        registry.put(B.class, "B");
        assertNull(registry.get(Object.class));
        assertEquals("A", registry.get(A.class));
        assertEquals("B", registry.get(B.class));
    }

    @Test
    public void registryWithObjectAndAAndB()
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
