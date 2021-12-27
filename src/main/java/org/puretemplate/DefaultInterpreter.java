package org.puretemplate;

import java.io.IOException;
import java.util.Locale;

import org.puretemplate.error.ErrorType;

class DefaultInterpreter extends AbstractInterpreter
{
    public DefaultInterpreter(STGroup group, Locale locale, ErrorManager errMgr)
    {
        super(group, locale, errMgr);
    }

    @Override
    protected int writeText(Job job, InstanceScope scope, String o)
    {
        TemplateWriter writer = job.getTemplateWriter();
        int start = writer.index(); // track char we're about to write
        int n = writeTextObject(writer, scope, o);
        fireEvent(job,
            () -> new EvalExpressionEvent(scope.toLocation(),
                start,
                writer.index() - 1,
                getExprStartChar(scope),
                getExprStopChar(scope)),
            ListenerInvoker.EVAL_EXPRESSION);
        return n;
    }

    protected int writeTextObject(TemplateWriter out, InstanceScope scope, String v)
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
