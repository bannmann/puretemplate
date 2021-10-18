package org.puretemplate;

public final class Loader
{
    public static class TemplateLoader extends TemplateLoader0
    {
        TemplateLoader()
        {
            super(new TemplateLoaderAction());
        }
    }

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
