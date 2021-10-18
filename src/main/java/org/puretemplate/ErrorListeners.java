package org.puretemplate;

import lombok.experimental.UtilityClass;

import org.puretemplate.error.ErrorListener;
import org.puretemplate.error.ErrorType;
import org.puretemplate.error.Message;

@UtilityClass
class ErrorListeners
{
    @SuppressWarnings("java:S106")
    public static final ErrorListener SYSTEM_ERR = new ErrorListener()
    {
        @Override
        public void compileTimeError(Message msg)
        {
            System.err.println(msg);
        }

        @Override
        public void runTimeError(Message msg)
        {
            if (msg.getError() != ErrorType.NO_SUCH_PROPERTY)
            {
                // ignore these
                System.err.println(msg);
            }
        }

        @Override
        public void ioError(Message msg)
        {
            System.err.println(msg);
        }

        @Override
        public void internalError(Message msg)
        {
            System.err.println(msg);
        }
    };
}
