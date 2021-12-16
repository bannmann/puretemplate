package org.puretemplate;

import org.apiguardian.api.API;
import org.puretemplate.model.AttributeRenderer;

@API(status = API.Status.STABLE)
public final class Loader
{
    /**
     * Defines the different ways an {@link AttributeRenderer} can be registered.
     */
    @API(status = API.Status.STABLE)
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
         */
        RECURSIVE
            {
                // TODO do this without changing the state (configuration) of those groups (maybe by cloning them all?)
                @Override
                <T> void register(STGroup stGroup, Class<T> type, AttributeRenderer<? super T> renderer)
                {
                    stGroup.registerRenderer(type, renderer, true);
                }
            };

        abstract <T> void register(STGroup stGroup, Class<T> type, AttributeRenderer<? super T> renderer);
    }

    @API(status = API.Status.STABLE)
    public static class TemplateLoader extends TemplateLoader0
    {
        TemplateLoader()
        {
            super(new TemplateLoaderAction());
        }
    }

    @API(status = API.Status.STABLE)
    public static class GroupLoader extends GroupLoader0
    {
        GroupLoader()
        {
            super(new GroupLoaderAction());
        }
    }

    /**
     * Loads a template.
     *
     * @return the initial state of the fluent API. See <a href="package-summary.html#fluent-api-usage-notes">Usage
     * notes for fluent APIs in PureTemplate</a> for details.
     */
    public TemplateLoader getTemplate()
    {
        return new TemplateLoader();
    }

    /**
     * Loads a group.
     *
     * @return the initial state of the fluent API. See <a href="package-summary.html#fluent-api-usage-notes">Usage
     * notes for fluent APIs in PureTemplate</a> for details.
     */
    public GroupLoader getGroup()
    {
        return new GroupLoader();
    }
}
