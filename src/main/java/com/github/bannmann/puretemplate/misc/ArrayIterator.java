package com.github.bannmann.puretemplate.misc;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator for an array so I don't have to copy the array to a {@link List} just to make it implement {@link
 * Iterator}.
 */
public class ArrayIterator implements Iterator<Object>
{
    /**
     * Index into the data array
     */
    protected int i = -1;
    protected Object array = null;
    /**
     * Arrays are fixed size; precompute.
     */
    protected int n;

    public ArrayIterator(Object array)
    {
        this.array = array;
        n = Array.getLength(array);
    }

    @Override
    public boolean hasNext()
    {
        return (i + 1) < n && n > 0;
    }

    @Override
    public Object next()
    {
        i++; // move to next element
        if (i >= n)
        {
            throw new NoSuchElementException();
        }
        return Array.get(array, i);
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
