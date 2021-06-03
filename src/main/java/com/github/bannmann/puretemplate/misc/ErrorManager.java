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

    public void lexerError(String srcName, String msg, Token templateToken, RecognitionException e)
    {
        if (srcName != null)
        {
            srcName = Misc.getFileName(srcName);
        }
        listener.compileTimeError(new STLexerMessage(srcName, msg, templateToken, e));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t)
    {
        String srcName = getSourceName(t);
        String text = t.getText();
        listener.compileTimeError(new STCompiletimeMessage(error, srcName, templateToken, t, null, text));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t, Object arg)
    {
        String srcName = getSourceName(t);
        listener.compileTimeError(new STCompiletimeMessage(error, srcName, templateToken, t, null, arg));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t, Object arg, Object arg2)
    {
        String srcName = getSourceName(t);
        listener.compileTimeError(new STCompiletimeMessage(error, srcName, templateToken, t, null, arg, arg2));
    }

    public void groupSyntaxError(ErrorType error, String srcName, RecognitionException e, String msg)
    {
        Token token = getToken(e);
        listener.compileTimeError(new STGroupCompiletimeMessage(error, srcName, token, e, msg));
    }

    public void groupLexerError(ErrorType error, String srcName, RecognitionException e, String msg)
    {
        Token token = getToken(e);
        listener.compileTimeError(new STGroupCompiletimeMessage(error, srcName, token, e, msg));
    }

    public void runTimeError(Interpreter interp, InstanceScope scope, ErrorType error)
    {
        int ip = getIp(scope);
        listener.runTimeError(new STRuntimeMessage(interp, error, ip, scope));
    }

    public void runTimeError(Interpreter interp, InstanceScope scope, ErrorType error, Throwable e, Object arg)
    {
        int ip = getIp(scope);
        listener.runTimeError(new STRuntimeMessage(interp, error, ip, scope, e, arg));
    }

    public void runTimeError(Interpreter interp, InstanceScope scope, ErrorType error, Object arg)
    {
        int ip = getIp(scope);
        listener.runTimeError(new STRuntimeMessage(interp, error, ip, scope, arg));
    }

    public void runTimeError(Interpreter interp, InstanceScope scope, ErrorType error, Object arg, Object arg2)
    {
        int ip = getIp(scope);
        listener.runTimeError(new STRuntimeMessage(interp, error, ip, scope, null, arg, arg2));
    }

    public void runTimeError(
        Interpreter interp, InstanceScope scope, ErrorType error, Object arg, Object arg2, Object arg3)
    {
        int ip = getIp(scope);
        listener.runTimeError(new STRuntimeMessage(interp, error, ip, scope, null, arg, arg2, arg3));
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

    private String getSourceName(Token t)
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

    private Token getToken(RecognitionException e)
    {
        return e.token;
    }

    private int getIp(InstanceScope scope)
    {
        if (scope != null)
        {
            return scope.ip;
        }
        return 0;
    }
}
