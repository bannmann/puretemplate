package org.puretemplate;

import lombok.Value;

import org.puretemplate.model.ModelAdaptor;

@Value
class AdaptorHandle<T> implements Handle
{
    Class<T> type;
    ModelAdaptor<? super T> adaptor;

    @Override
    public void registerWith(STGroup stGroup)
    {
        stGroup.registerModelAdaptor(type, adaptor);
    }
}
