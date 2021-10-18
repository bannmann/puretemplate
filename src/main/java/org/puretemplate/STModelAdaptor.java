package org.puretemplate;

import org.puretemplate.exception.NoSuchPropertyException;
import org.puretemplate.model.ModelAdaptor;

class STModelAdaptor implements ModelAdaptor<ST>
{
    @Override
    public Object getProperty(ST model, Object property, String propertyName) throws NoSuchPropertyException
    {
        return model.getAttribute(propertyName);
    }
}
