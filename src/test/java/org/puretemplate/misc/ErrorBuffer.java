package org.puretemplate.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.puretemplate.error.ErrorListener;
import org.puretemplate.error.ErrorType;
import org.puretemplate.error.Message;

/**
 * Used during tests to track all errors.
 */
public class ErrorBuffer implements ErrorListener
{
    private static final String NEWLINE = System.getProperty("line.separator");

    protected final List<Message> errors = new ArrayList<>();

    public List<Message> getErrors()
    {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public void compileTimeError(Message msg)
    {
        errors.add(msg);
    }

    @Override
    public void runTimeError(Message msg)
    {
        if (msg.getError() != ErrorType.NO_SUCH_PROPERTY)
        {
            // ignore these
            errors.add(msg);
        }
    }

    @Override
    public void ioError(Message msg)
    {
        errors.add(msg);
    }

    @Override
    public void internalError(Message msg)
    {
        errors.add(msg);
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        for (Message message : errors)
        {
            buf.append(message)
                .append(NEWLINE);
        }
        return buf.toString();
    }
}
