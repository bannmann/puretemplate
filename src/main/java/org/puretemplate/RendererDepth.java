package org.puretemplate;

import org.puretemplate.model.AttributeRenderer;

public enum RendererDepth
{
    /**
     * Register the renderer only for the top-level group.
     */
    NON_RECURSIVE
        {
            @Override
            <T> void register(STGroup stGroup, Class<T> type, AttributeRenderer<? super T> renderer)
            {
                stGroup.registerRenderer(type, renderer, false);
            }
        },

    /**
     * Recursively register the renderer for all imported groups.
     *
     * TODO figure out how to do this without changing the state of those groups (when used directly)
     */
    RECURSIVE
        {
            @Override
            <T> void register(STGroup stGroup, Class<T> type, AttributeRenderer<? super T> renderer)
            {
                stGroup.registerRenderer(type, renderer, true);
            }
        };

    abstract <T> void register(STGroup stGroup, Class<T> type, AttributeRenderer<? super T> renderer);
}
