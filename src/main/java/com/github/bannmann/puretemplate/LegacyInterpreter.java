package com.github.bannmann.puretemplate;

import java.util.Locale;

import com.github.bannmann.puretemplate.misc.ErrorManager;

class LegacyInterpreter extends AbstractInterpreter
{
    public LegacyInterpreter(STGroup group, Locale locale, ErrorManager errMgr, boolean debug)
    {
        super(group, locale, errMgr, debug);
    }

    @Override
    protected int writeText(STWriter out, InstanceScope scope, String o)
    {
        return writeObjectNoOptions(out, scope, o);
    }
}
