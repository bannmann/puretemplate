package org.puretemplate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.puretemplate.error.ErrorListener;
import org.puretemplate.error.ErrorType;
import org.puretemplate.error.GroupCompilationMessage;
import org.puretemplate.error.LexerMessage;
import org.puretemplate.error.Message;
import org.puretemplate.error.RuntimeMessage;
import org.puretemplate.error.TemplateCompilationMessage;
import org.puretemplate.misc.Location;

@RequiredArgsConstructor
class ErrorManager
{
    @NonNull
    public final ErrorListener listener;

    public void lexerError(String srcName, String msg, Token templateToken, RecognitionException e)
    {
        if (srcName != null)
        {
            srcName = Misc.getFileName(srcName);
        }
        listener.compileTimeError(new LexerMessage(srcName, msg, templateToken, e));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t)
    {
        String srcName = getSourceName(t);
        String text = t.getText();
        listener.compileTimeError(new TemplateCompilationMessage(error, srcName, templateToken, t, null, text));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t, Object arg)
    {
        String srcName = getSourceName(t);
        listener.compileTimeError(new TemplateCompilationMessage(error, srcName, templateToken, t, null, arg));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t, Object arg, Object arg2)
    {
        String srcName = getSourceName(t);
        listener.compileTimeError(new TemplateCompilationMessage(error, srcName, templateToken, t, null, arg, arg2));
    }

    public void groupSyntaxError(ErrorType error, String srcName, RecognitionException e, String msg)
    {
        Token token = getToken(e);
        listener.compileTimeError(new GroupCompilationMessage(error, srcName, token, e, msg));
    }

    public void groupLexerError(ErrorType error, String srcName, RecognitionException e, String msg)
    {
        Token token = getToken(e);
        listener.compileTimeError(new GroupCompilationMessage(error, srcName, token, e, msg));
    }

    public void runTimeError(Location location, ErrorType error)
    {
        listener.runTimeError(new RuntimeMessage(error, location));
    }

    public void runTimeError(Location location, ErrorType error, Throwable e, Object arg)
    {
        listener.runTimeError(new RuntimeMessage(error, location, e, arg));
    }

    public void runTimeError(Location location, ErrorType error, Object arg)
    {
        listener.runTimeError(new RuntimeMessage(error, location, arg));
    }

    public void runTimeError(Location location, ErrorType error, Object arg, Object arg2)
    {
        listener.runTimeError(new RuntimeMessage(error, location, null, arg, arg2));
    }

    public void runTimeError(Location location, ErrorType error, Object arg, Object arg2, Object arg3)
    {
        listener.runTimeError(new RuntimeMessage(error, location, null, arg, arg2, arg3));
    }

    public void ioError(Location location, ErrorType error, Throwable e)
    {
        listener.ioError(new Message(error, location, e));
    }

    public void ioError(Location location, ErrorType error, Throwable e, Object arg)
    {
        listener.ioError(new Message(error, location, e, arg));
    }

    public void internalError(Location location, String msg, Throwable e)
    {
        listener.internalError(new Message(ErrorType.INTERNAL_ERROR, location, e, msg));
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
}
