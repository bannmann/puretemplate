package org.puretemplate;

import java.util.Locale;

class LegacyInterpreter extends AbstractInterpreter
{
    public LegacyInterpreter(STGroup group, Locale locale, ErrorManager errMgr)
    {
        super(group, locale, errMgr);
    }

    @Override
    protected int writeText(Job job, InstanceScope scope, String o)
    {
        return writeObjectNoOptions(job, scope, o);
    }
}
