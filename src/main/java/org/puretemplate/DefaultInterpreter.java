package org.puretemplate;

import java.io.IOException;
import java.util.Locale;

import org.puretemplate.error.ErrorType;

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
            EvalExprEvent e = new EvalExprEvent(scope.toLocation(),
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
            errMgr.ioError(scope.toLocation(), ErrorType.WRITE_IO_ERROR, ioe, v);
            return 0;
        }
    }
}
