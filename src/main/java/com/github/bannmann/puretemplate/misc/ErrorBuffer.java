package com.github.bannmann.puretemplate.misc;

import java.util.ArrayList;
import java.util.List;

import com.github.bannmann.puretemplate.STErrorListener;

/**
 * Used during tests to track all errors.
 */
public class ErrorBuffer implements STErrorListener
{
    public List<STMessage> errors = new ArrayList<STMessage>();

    @Override
    public void compileTimeError(STMessage msg)
    {
        errors.add(msg);
    }

    @Override
    public void runTimeError(STMessage msg)
    {
        if (msg.error != ErrorType.NO_SUCH_PROPERTY)
        {
            // ignore these
            errors.add(msg);
        }
    }

    @Override
    public void IOError(STMessage msg)
    {
        errors.add(msg);
    }

    @Override
    public void internalError(STMessage msg)
    {
        errors.add(msg);
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        for (STMessage m : errors)
        {
            buf.append(m.toString() + Misc.newline);
        }
        return buf.toString();
    }
}
