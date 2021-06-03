package com.github.bannmann.puretemplate;

import java.io.IOException;
import java.util.Locale;

import com.github.bannmann.puretemplate.debug.EvalExprEvent;
import com.github.bannmann.puretemplate.misc.ErrorManager;
import com.github.bannmann.puretemplate.misc.ErrorType;

class DefaultInterpreter extends AbstractInterpreter
{
    public DefaultInterpreter(STGroup group, Locale locale, ErrorManager errMgr, boolean debug)
    {
        super(group, locale, errMgr, debug);
    }

    @Override
    protected int writeText(STWriter out, InstanceScope scope, String o)
    {
        int start = out.index(); // track char we're about to write
        int n = writeTextObject(out, scope, o);
        if (debug)
        {
            EvalExprEvent e = new EvalExprEvent(scope,
                start,
                out.index() - 1,
                getExprStartChar(scope),
                getExprStopChar(scope));
            trackDebugEvent(scope, e);
        }
        return n;
    }

    protected int writeTextObject(STWriter out, InstanceScope scope, String v)
    {
        try
        {
            return out.write(v);
        }
        catch (IOException ioe)
        {
            errMgr.IOError(scope.st, ErrorType.WRITE_IO_ERROR, ioe, v);
            return 0;
        }
    }
}
