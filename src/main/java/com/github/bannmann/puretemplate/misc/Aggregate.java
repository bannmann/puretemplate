package com.github.bannmann.puretemplate.misc;

import java.util.HashMap;

/**
 * An automatically created aggregate of properties.
 *
 * <p>I often have lists of things that need to be formatted, but the list
 * items are actually pieces of data that are not already in an object.  I need ST to do something like:</p>
 * <pre>{@code Ter=3432
 * Tom=32234
 * ....}</pre>
 * <p>
 * using template:</p>
 * <pre>{@code $items:{it.name$=$it.type$}$}</pre>
 * <p>
 * This example will call {@code getName()} on the objects in items attribute, but what if they aren't objects?  I have
 * perhaps two parallel arrays instead of a single array of objects containing two fields.  One solution is allow {@code
 * Map}s to be handled like properties so that {@code it.name} would fail {@code getName()} but then see that it's a
 * {@code Map} and do {@code it.get("name")} instead.</p>
 * <p>
 * This very clean approach is espoused by some, but the problem is that it's a hole in my separation rules.  People can
 * put the logic in the view because you could say: "go get bob's data" in the view:</p>
 * <pre>{@code Bob's Phone: $db.bob.phone$}</pre>
 * <p>
 * A view should not be part of the program and hence should never be able to go ask for a specific person's data.</p>
 * <p>
 * After much thought, I finally decided on a simple solution.  I've added setAttribute variants that pass in multiple
 * property values, with the property names specified as part of the name using a special attribute name syntax: {@code
 * "name.{propName1,propName2,...}"}.  This object is a special kind of {@code HashMap} that hopefully prevents people
 * from passing a subclass or other variant that they have created as it would be a loophole.  Anyway, the {@link
 * AggregateModelAdaptor#getProperty} method looks for {@code Aggregate} as a special case and does a {@link #get}
 * instead of {@code getPropertyName}.</p>
 */
public class Aggregate
{
    public HashMap<String, Object> properties = new HashMap<String, Object>();

    /**
     * Allow StringTemplate to add values, but prevent the end user from doing so.
     */
    protected void put(String propName, Object propValue)
    {
        properties.put(propName, propValue);
    }

    public Object get(String propName)
    {
        return properties.get(propName);
    }

    @Override
    public String toString()
    {
        return properties.toString();
    }
}
