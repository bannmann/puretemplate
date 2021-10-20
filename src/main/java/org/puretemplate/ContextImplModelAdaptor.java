package org.puretemplate;

import org.puretemplate.model.ModelAdaptor;

class ContextImplModelAdaptor implements ModelAdaptor<ContextImpl>
{
    @Override
    public Object getProperty(ContextImpl model, Object property, String propertyName)
    {
        return model.getAttribute(propertyName);
    }
}
