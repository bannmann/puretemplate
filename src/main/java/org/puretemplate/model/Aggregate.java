package org.puretemplate.model;

import java.util.HashMap;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.apiguardian.api.API;

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
 * This very clean approach is espoused by some, but the problem is that it's a hole in our model/view separation rules.
 * People can put the logic in the view because you could say: "go get bob's data" in the view:</p>
 * <pre>{@code Bob's Phone: $db.bob.phone$}</pre>
 * <p>
 * A view should not be part of the program and hence should never be able to go ask for a specific person's data.</p>
 * <p>
 * The solution is this class. Use {@link #build()} to construct an Aggregate like this:
 * <pre>{@code Aggregate.build()
 *     .properties("firstName", "lastName", "id")
 *     .withValues("Tom", "Burns", 34)}</pre>
 * Then, use {@link org.puretemplate.Context#add(String, Object)} to add the aggregate just like any other object.</p>
 */
@API(status = API.Status.STABLE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Aggregate
{
    @API(status = API.Status.STABLE)
    public static final class AggregateBuilder extends AggregateBuilder0
    {
        private AggregateBuilder()
        {
            super(new AggregateBuilderAction());
        }
    }

    /**
     * Builds an Aggregate.
     *
     * @return the initial state of the fluent API. See <a href="package-summary.html#fluent-api-usage-notes">Usage
     * notes for fluent APIs in PureTemplate</a> for details.
     */
    public static AggregateBuilder build()
    {
        return new AggregateBuilder();
    }

    /**
     * Relaxed visibility for {@link AggregateModelAdaptor}
     */
    HashMap<String, Object> properties = new HashMap<>();

    /**
     * Allow {@link AggregateBuilder} to add values, but prevent the end user from doing so.
     */
    void put(String propName, Object propValue)
    {
        properties.put(propName, propValue);
    }

    public Object get(String propertyName)
    {
        return properties.get(propertyName);
    }

    @Override
    public String toString()
    {
        return properties.toString();
    }
}
