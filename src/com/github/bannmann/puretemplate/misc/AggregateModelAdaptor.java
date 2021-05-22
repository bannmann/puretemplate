package com.github.bannmann.puretemplate.misc;

import com.github.bannmann.puretemplate.Interpreter;
import com.github.bannmann.puretemplate.ModelAdaptor;
import com.github.bannmann.puretemplate.ST;

/** Deal with structs created via {@link ST#addAggr}{@code ("structname.{prop1, prop2}", ...);}. */
public class AggregateModelAdaptor implements ModelAdaptor<Aggregate> {
    private final MapModelAdaptor mapAdaptor = new MapModelAdaptor();

    @Override
    public Object getProperty(Interpreter interp, ST self, Aggregate o, Object property, String propertyName)
        throws STNoSuchPropertyException
    {
        return mapAdaptor.getProperty(interp, self, o.properties, property, propertyName);
    }
}
