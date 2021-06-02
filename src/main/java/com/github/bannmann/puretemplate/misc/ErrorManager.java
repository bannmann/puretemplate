package com.github.bannmann.puretemplate.misc;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;

import com.github.bannmann.puretemplate.InstanceScope;
import com.github.bannmann.puretemplate.Interpreter;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STErrorListener;

public class ErrorManager
{
    public static STErrorListener DEFAULT_ERROR_LISTENER = new STErrorListener()
    {
        @Override
        public void compileTimeError(STMessage msg)
        {
            System.err.println(msg);
        }

        @Override
        public void runTimeError(STMessage msg)
        {
            if (msg.error != ErrorType.NO_SUCH_PROPERTY)
            {
                // ignore these
                System.err.println(msg);
            }
        }

        @Override
        public void IOError(STMessage msg)
        {
            System.err.println(msg);
        }

        @Override
        public void internalError(STMessage msg)
        {
            System.err.println(msg);
        }

        public void error(String s)
        {
            error(s, null);
        }

        public void error(String s, Throwable e)
        {
            System.err.println(s);
            if (e != null)
            {
                e.printStackTrace(System.err);
            }
        }
    };

    public final STErrorListener listener;

    public ErrorManager()
    {
        this(DEFAULT_ERROR_LISTENER);
    }

    public ErrorManager(STErrorListener listener)
    {
        this.listener = listener;
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t)
    {
        String srcName = sourceName(t);
        listener.compileTimeError(new STCompiletimeMessage(error, srcName, templateToken, t, null, t.getText()));
    }

    public void lexerError(String srcName, String msg, Token templateToken, RecognitionException e)
    {
        if (srcName != null)
        {
            srcName = Misc.getFileName(srcName);
        }
        listener.compileTimeError(new STLexerMessage(srcName, msg, templateToken, e));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t, Object arg)
    {
        String srcName = sourceName(t);
        listener.compileTimeError(new STCompiletimeMessage(error, srcName, templateToken, t, null, arg));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t, Object arg, Object arg2)
    {
        String srcName = sourceName(t);
        listener.compileTimeError(new STCompiletimeMessage(error, srcName, templateToken, t, null, arg, arg2));
    }

    public void groupSyntaxError(ErrorType error, String srcName, RecognitionException e, String msg)
    {
        Token t = e.token;
        listener.compileTimeError(new STGroupCompiletimeMessage(error, srcName, e.token, e, msg));
    }

    public void groupLexerError(ErrorType error, String srcName, RecognitionException e, String msg)
    {
        listener.compileTimeError(new STGroupCompiletimeMessage(error, srcName, e.token, e, msg));
    }

    public void runTimeError(Interpreter interp, InstanceScope scope, ErrorType error)
    {
        listener.runTimeError(new STRuntimeMessage(interp,
            error,
            scope != null
                ? scope.ip
                : 0,
            scope));
    }

    public void runTimeError(Interpreter interp, InstanceScope scope, ErrorType error, Object arg)
    {
        listener.runTimeError(new STRuntimeMessage(interp,
            error,
            scope != null
                ? scope.ip
                : 0,
            scope,
            arg));
    }

    public void runTimeError(Interpreter interp, InstanceScope scope, ErrorType error, Throwable e, Object arg)
    {
        listener.runTimeError(new STRuntimeMessage(interp,
            error,
            scope != null
                ? scope.ip
                : 0,
            scope,
            e,
            arg));
    }

    public void runTimeError(Interpreter interp, InstanceScope scope, ErrorType error, Object arg, Object arg2)
    {
        listener.runTimeError(new STRuntimeMessage(interp,
            error,
            scope != null
                ? scope.ip
                : 0,
            scope,
            null,
            arg,
            arg2));
    }

    public void runTimeError(
        Interpreter interp, InstanceScope scope, ErrorType error, Object arg, Object arg2, Object arg3)
    {
        listener.runTimeError(new STRuntimeMessage(interp,
            error,
            scope != null
                ? scope.ip
                : 0,
            scope,
            null,
            arg,
            arg2,
            arg3));
    }

    public void IOError(ST self, ErrorType error, Throwable e)
    {
        listener.IOError(new STMessage(error, self, e));
    }

    public void IOError(ST self, ErrorType error, Throwable e, Object arg)
    {
        listener.IOError(new STMessage(error, self, e, arg));
    }

    public void internalError(ST self, String msg, Throwable e)
    {
        listener.internalError(new STMessage(ErrorType.INTERNAL_ERROR, self, e, msg));
    }

    private String sourceName(Token t)
    {
        CharStream input = t.getInputStream();
        if (input == null)
        {
            return null;
        }
        String srcName = input.getSourceName();
        if (srcName != null)
        {
            srcName = Misc.getFileName(srcName);
        }
        return srcName;
    }
}
