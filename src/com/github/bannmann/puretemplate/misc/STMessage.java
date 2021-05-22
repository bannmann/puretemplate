package com.github.bannmann.puretemplate.misc;

import org.antlr.runtime.Token;
import com.github.bannmann.puretemplate.ST;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Upon error, ST creates an {@link STMessage} or subclass instance and notifies
 *  the listener.  This root class is used for IO and internal errors.
 *
 *  @see STRuntimeMessage
 *  @see STCompiletimeMessage
 */
public class STMessage {
    /** if in debug mode, has created instance, add attr events and eval
     *  template events.
     */
    public ST self;
    public ErrorType error;
    public Object arg;
    public Object arg2;
    public Object arg3;
    public Throwable cause;

    public STMessage(ErrorType error) {
        this.error = error;
    }
    public STMessage(ErrorType error, ST self) {
        this(error);
        this.self = self;
    }
    public STMessage(ErrorType error, ST self, Throwable cause) {
        this(error,self);
        this.cause = cause;
    }
    public STMessage(ErrorType error, ST self, Throwable cause, Object arg) {
        this(error,self,cause);
        this.arg = arg;
    }
    public STMessage(ErrorType error, ST self, Throwable cause, Token where, Object arg) {
        this(error,self,cause,where);
        this.arg = arg;
    }
    public STMessage(ErrorType error, ST self, Throwable cause, Object arg, Object arg2) {
        this(error,self,cause,arg);
        this.arg2 = arg2;
    }
    public STMessage(ErrorType error, ST self, Throwable cause, Object arg, Object arg2, Object arg3) {
        this(error,self,cause,arg,arg2);
        this.arg3 = arg3;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String msg = String.format(error.message, arg, arg2, arg3);
        pw.print(msg);
        if ( cause!=null ) {
            pw.print("\nCaused by: ");
            cause.printStackTrace(pw);
        }
        return sw.toString();
    }
}
