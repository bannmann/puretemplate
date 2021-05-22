package com.github.bannmann.puretemplate.misc;

import com.github.bannmann.puretemplate.Interpreter;
import com.github.bannmann.puretemplate.ModelAdaptor;
import com.github.bannmann.puretemplate.ST;

public class STModelAdaptor implements ModelAdaptor<ST> {
    @Override
    public Object getProperty(Interpreter interp, ST self, ST model, Object property, String propertyName)
        throws STNoSuchPropertyException
    {
        return model.getAttribute(propertyName);
    }
}
