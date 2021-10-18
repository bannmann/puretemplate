package org.puretemplate;

class TemplateLoaderAction extends AbstractLoaderAction implements ITemplateLoaderAction
{
    private Group parentGroup;

    @Override
    public void attachedToGroup(Group group)
    {
        parentGroup = group;
    }

    @Override
    public Template build()
    {
        // Read InputStreams/Readers now to ensure that TemplateImpl can invoke its supplier multiple times
        String sourceText = loadFrom(source);

        return new TemplateImpl(() -> createSt(sourceText));
    }

    private ST createSt(String sourceText)
    {
        // Parent group is mutually exclusive with specifying delimiters
        if (parentGroup != null)
        {
            InternalGroup parentGroupInternal = (InternalGroup) this.parentGroup;
            return new ST(parentGroupInternal.getStGroup(), sourceText);
        }
        else
        {
            return new ST(sourceText, delimiterConfig.getStart(), delimiterConfig.getStop());
        }
    }
}
