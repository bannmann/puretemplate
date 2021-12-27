package org.puretemplate;

@SuppressWarnings("java:S1694")
abstract class AbstractEvent
{
    /**
     * Ensure each non-abstract subclass adds a specific implementation instead of inheriting it from {@link Object}.
     */
    @Override
    public abstract String toString();
}
