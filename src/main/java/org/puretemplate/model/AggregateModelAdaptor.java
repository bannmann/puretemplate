package org.puretemplate.model;

import org.apiguardian.api.API;
import org.puretemplate.exception.NoSuchPropertyException;

/**
 * Deal with structs created via {@link Aggregate#build()}.
 */
@API(status = API.Status.STABLE)
public class AggregateModelAdaptor implements ModelAdaptor<Aggregate>
{
    private final MapModelAdaptor mapAdaptor = new MapModelAdaptor();

    @Override
    public Object getProperty(Aggregate o, Object property, String propertyName) throws NoSuchPropertyException
    {
        return mapAdaptor.getProperty(o.properties, property, propertyName);
    }
}
