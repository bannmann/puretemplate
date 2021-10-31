package org.puretemplate.model;

import java.util.Map;

import org.apiguardian.api.API;
import org.puretemplate.exception.NoSuchPropertyException;

/**
 * An object that knows how to convert property references to appropriate actions on a model object. Some models, like
 * JDBC, are interface based (we aren't supposed to care about implementation classes). Some other models don't follow
 * StringTemplate's getter method naming convention. So, if we have an object of type {@code M} with property method
 * {@code M.foo()} (as opposed to {@code M.getFoo()}), we can register a model adaptor object, {@code adap}, that
 * converts a lookup for property {@code foo} into a call to {@code M.foo()}.<br>
 * <br>
 * Given {@code <a.foo>}, we look up {@code foo} via the adaptor if {@code a instanceof M}.<br>
 * <br>
 * <b>API status:</b> {@link API.Status#STABLE}, but {@link #getProperty(Object, Object, String)} is
 * {@link API.Status#MAINTAINED}.
 *
 * @param <T> the type of values this adaptor can handle.
 */
@API(status = API.Status.STABLE)
public interface ModelAdaptor<T>
{
    /**
     * Lookup property name in {@code o} and return its value. <br>
     * <br>
     * {@code property} is normally a {@code String} but doesn't have to be. E.g., if {@code o} is {@link Map}, {@code
     * property} could be any key type. If we need to convert to {@code String}, then it's done by {@code ST} and passed
     * in here.<br>
     * <br>
     * <b>API status:</b> {@link API.Status#MAINTAINED} because passing the property as both an {@link Object} and a
     * {@link String} could be replaced by an {@link Object}-only variant. Feedback welcome.
     */
    @API(status = API.Status.MAINTAINED)
    Object getProperty(T model, Object property, String propertyName) throws NoSuchPropertyException;
}
