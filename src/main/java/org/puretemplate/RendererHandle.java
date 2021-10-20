package org.puretemplate;

import lombok.Value;

import org.puretemplate.model.AttributeRenderer;

@Value
class RendererHandle<T> implements Handle
{
    Class<T> type;
    AttributeRenderer<? super T> renderer;
    RendererDepth depth;

    @Override
    public void registerWith(STGroup stGroup)
    {
        depth.register(stGroup, type, renderer);
    }
}
