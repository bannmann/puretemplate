package org.puretemplate;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.puretemplate.error.ErrorType;

/**
 * A group derived from a string not a file or directory.
 */
class STGroupString extends GreenfieldStGroup
{
    private final String sourceName;
    private final String text;
    protected boolean alreadyLoaded;

    public STGroupString(String text)
    {
        this("<string>", text, '<', '>');
    }

    public STGroupString(String sourceName, String text, char delimiterStartChar, char delimiterStopChar)
    {
        super(delimiterStartChar, delimiterStopChar);
        this.sourceName = sourceName;
        this.text = text;
    }

    @Override
    public boolean isDictionary(String name)
    {
        if (!alreadyLoaded)
        {
            load();
        }
        return super.isDictionary(name);
    }

    @Override
    public boolean isDefined(String name)
    {
        if (!alreadyLoaded)
        {
            load();
        }
        return super.isDefined(name);
    }

    @Override
    protected synchronized CompiledST load(String name)
    {
        if (!alreadyLoaded)
        {
            load();
        }
        return rawGetTemplate(name);
    }

    @Override
    public synchronized void load()
    {
        if (alreadyLoaded)
        {
            return;
        }
        alreadyLoaded = true;
        GroupParser parser;
        try
        {
            ANTLRStringStream fs = new ANTLRStringStream(text);
            fs.name = sourceName;
            GroupLexer lexer = new GroupLexer(fs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new GroupParser(tokens);
            // no prefix since this group file is the entire group, nothing lives
            // beneath it.
            parser.group(this, "/");
        }
        catch (Exception e)
        {
            errMgr.ioError(null, ErrorType.CANT_LOAD_GROUP_FILE, e, "<string>");
        }
    }

    @Override
    public String getFileName()
    {
        return sourceName;
    }
}
