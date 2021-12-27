package org.puretemplate;

import java.io.PrintWriter;
import java.io.StringWriter;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
    @Getter
    @AllArgsConstructor
    private static class MessageImpl implements Message
    {
        protected final ErrorType error;
        protected final Location location;
        protected final Throwable cause;
        protected final Object arg;
        protected final Object arg2;
        protected final Object arg3;

        public MessageImpl(ErrorType error, Location location, Throwable cause)
        {
            this(error, location, cause, null, null, null);
        }

        public MessageImpl(ErrorType error, Location location, Throwable cause, Object arg)
        {
            this(error, location, cause, arg, null, null);
        }

        public MessageImpl(ErrorType error, Location location, Throwable cause, Object arg, Object arg2)
        {
            this(error, location, cause, arg, arg2, null);
        }

        @Override
        public String toString()
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            String msg = String.format(error.getMessage(), arg, arg2, arg3);
            pw.print(msg);
            if (cause != null)
            {
                pw.print("\nCaused by: ");
                cause.printStackTrace(pw);
            }
            return sw.toString();
        }
    }

    @Getter
    private static final class GroupCompilationMessageImpl extends MessageImpl implements GroupCompilationMessage
    {
        /**
         * token inside group file
         */
        private final Token token;

        private final String sourceName;

        /**
         * Creates
         */
        public GroupCompilationMessageImpl(
            ErrorType errorType, String sourceName, Token token, Throwable cause, Object arg)
        {
            super(errorType, null, cause, arg);
            this.token = token;
            this.sourceName = sourceName;
        }

        @Override
        public String toString()
        {
            RecognitionException re = (RecognitionException) cause;
            int line = 0;
            int charPos = -1;
            if (token != null)
            {
                line = token.getLine();
                charPos = token.getCharPositionInLine();
            }
            else if (re != null)
            {
                line = re.line;
                charPos = re.charPositionInLine;
            }
            String filepos = line + ":" + charPos;
            if (sourceName != null)
            {
                return sourceName + " " + filepos + ": " + String.format(error.getMessage(), arg, arg2);
            }
            return filepos + ": " + String.format(error.getMessage(), arg, arg2);
        }
    }

    @Getter
    private static final class LexerMessageImpl extends MessageImpl implements LexerMessage
    {
        private final String message;

        /**
         * overall token pulled from group file
         */
        private final Token templateToken;

        private final String sourceName;

        public LexerMessageImpl(String sourceName, String message, Token templateToken, RecognitionException cause)
        {
            super(ErrorType.LEXER_ERROR, null, cause, null);
            this.message = message;
            this.templateToken = templateToken;
            this.sourceName = sourceName;
        }

        @Override
        public RecognitionException getCause()
        {
            return (RecognitionException) super.getCause();
        }

        @Override
        public String toString()
        {
            RecognitionException re = getCause();
            int line = re.line;
            int charPos = re.charPositionInLine;
            if (templateToken != null)
            {
                line += templateToken.getLine() - 1;
                charPos += templateToken.getCharPositionInLine() + Parsing.getTemplateDelimiterSize(templateToken);
            }
            String filepos = line + ":" + charPos;
            if (sourceName != null)
            {
                return sourceName + " " + filepos + ": " + String.format(error.getMessage(), message);
            }
            return filepos + ": " + String.format(error.getMessage(), message);
        }
    }

    private static final class RuntimeMessageImpl extends MessageImpl implements RuntimeMessage
    {
        public RuntimeMessageImpl(ErrorType error, Location location)
        {
            this(error, location, null);
        }

        public RuntimeMessageImpl(ErrorType error, Location location, Object arg)
        {
            this(error, location, null, arg, null);
        }

        public RuntimeMessageImpl(ErrorType error, Location location, Throwable e, Object arg)
        {
            this(error, location, e, arg, null);
        }

        public RuntimeMessageImpl(ErrorType error, Location location, Throwable e, Object arg, Object arg2)
        {
            this(error, location, e, arg, arg2, null);
        }

        public RuntimeMessageImpl(ErrorType error, Location location, Throwable e, Object arg, Object arg2, Object arg3)
        {
            super(error, location, e, arg, arg2, arg3);
        }

        @Override
        public String toString()
        {
            StringBuilder buf = new StringBuilder();
            if (location != null)
            {
                buf.append("context [")
                    .append(location.getCallHierarchy())
                    .append("]");

                location.getCoordinates()
                    .ifPresent(coordinate -> buf.append(" ")
                        .append(coordinate));
            }
            buf.append(" ")
                .append(super.toString());
            return buf.toString();
        }
    }

    @Getter
    public static final class TemplateCompilationMessageImpl extends MessageImpl implements TemplateCompilationMessage
    {
        /**
         * overall token pulled from group file
         */
        private final Token templateToken;

        /**
         * token inside template
         */
        private final Token token;

        private final String sourceName;

        public TemplateCompilationMessageImpl(
            ErrorType error, String sourceName, Token templateToken, Token t, Throwable cause, Object arg)
        {
            this(error, sourceName, templateToken, t, cause, arg, null);
        }

        public TemplateCompilationMessageImpl(
            ErrorType error, String sourceName, Token templateToken, Token t, Throwable cause, Object arg, Object arg2)
        {
            super(error, null, cause, arg, arg2);
            this.templateToken = templateToken;
            this.token = t;
            this.sourceName = sourceName;
        }

        @Override
        public String toString()
        {
            int line = 0;
            int charPos = -1;
            if (token != null)
            {
                line = token.getLine();
                charPos = token.getCharPositionInLine();
                // check the input streams - if different then token is embedded in templateToken and we need to adjust the offset
                if (templateToken != null &&
                    !templateToken.getInputStream()
                        .equals(token.getInputStream()))
                {
                    line += templateToken.getLine() - 1;
                    charPos += templateToken.getCharPositionInLine() + Parsing.getTemplateDelimiterSize(templateToken);
                }
            }
            String filepos = line + ":" + charPos;
            if (sourceName != null)
            {
                return sourceName + " " + filepos + ": " + String.format(error.getMessage(), arg, arg2);
            }
            return filepos + ": " + String.format(error.getMessage(), arg, arg2);
        }
    }

    @NonNull
    public final ErrorListener listener;

    public void lexerError(String sourceName, String msg, Token templateToken, RecognitionException e)
    {
        if (sourceName != null)
        {
            sourceName = Misc.getFileName(sourceName);
        }
        listener.compileTimeError(new LexerMessageImpl(sourceName, msg, templateToken, e));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t)
    {
        String sourceName = getSourceName(t);
        String text = t.getText();
        listener.compileTimeError(new TemplateCompilationMessageImpl(error, sourceName, templateToken, t, null, text));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t, Object arg)
    {
        String sourceName = getSourceName(t);
        listener.compileTimeError(new TemplateCompilationMessageImpl(error, sourceName, templateToken, t, null, arg));
    }

    public void compileTimeError(ErrorType error, Token templateToken, Token t, Object arg, Object arg2)
    {
        String sourceName = getSourceName(t);
        listener.compileTimeError(new TemplateCompilationMessageImpl(error,
            sourceName,
            templateToken,
            t,
            null,
            arg,
            arg2));
    }

    public void groupSyntaxError(ErrorType error, String sourceName, RecognitionException e, String msg)
    {
        Token token = getToken(e);
        listener.compileTimeError(new GroupCompilationMessageImpl(error, sourceName, token, e, msg));
    }

    public void groupLexerError(ErrorType error, String sourceName, RecognitionException e, String msg)
    {
        Token token = getToken(e);
        listener.compileTimeError(new GroupCompilationMessageImpl(error, sourceName, token, e, msg));
    }

    public void runTimeError(Location location, ErrorType error)
    {
        listener.runTimeError(new RuntimeMessageImpl(error, location));
    }

    public void runTimeError(Location location, ErrorType error, Throwable e, Object arg)
    {
        listener.runTimeError(new RuntimeMessageImpl(error, location, e, arg));
    }

    public void runTimeError(Location location, ErrorType error, Object arg)
    {
        listener.runTimeError(new RuntimeMessageImpl(error, location, arg));
    }

    public void runTimeError(Location location, ErrorType error, Object arg, Object arg2)
    {
        listener.runTimeError(new RuntimeMessageImpl(error, location, null, arg, arg2));
    }

    public void runTimeError(Location location, ErrorType error, Object arg, Object arg2, Object arg3)
    {
        listener.runTimeError(new RuntimeMessageImpl(error, location, null, arg, arg2, arg3));
    }

    public void ioError(Location location, ErrorType error, Throwable e)
    {
        listener.ioError(new MessageImpl(error, location, e));
    }

    public void ioError(Location location, ErrorType error, Throwable e, Object arg)
    {
        listener.ioError(new MessageImpl(error, location, e, arg));
    }

    public void internalError(Location location, String msg, Throwable e)
    {
        listener.internalError(new MessageImpl(ErrorType.INTERNAL_ERROR, location, e, msg));
    }

    private String getSourceName(Token t)
    {
        CharStream input = t.getInputStream();
        if (input == null)
        {
            return null;
        }
        String sourceName = input.getSourceName();
        if (sourceName != null)
        {
            sourceName = Misc.getFileName(sourceName);
        }
        return sourceName;
    }

    private Token getToken(RecognitionException e)
    {
        return e.token;
    }
}
