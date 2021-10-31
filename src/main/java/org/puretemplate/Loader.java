package org.puretemplate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class Loader
{
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

    public TemplateLoader getTemplate()
    {
        return new TemplateLoader();
    }

    public GroupLoader getGroup()
    {
        return new GroupLoader();
    }
}
