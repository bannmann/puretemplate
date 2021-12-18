package org.puretemplate;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.MessageFormat;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.puretemplate.error.ErrorType;

@Slf4j
class GreenfieldStGroup extends STGroup
{
    public GreenfieldStGroup(@NonNull Charset charset, char delimiterStartChar, char delimiterStopChar)
    {
        super(charset, delimiterStartChar, delimiterStopChar);
    }

    public GreenfieldStGroup(char delimiterStartChar, char delimiterStopChar)
    {
        super(delimiterStartChar, delimiterStopChar);
    }

    protected void loadGroupFile(Path filePath, InputStream inputStream, String prefix)
    {
        log.debug("loadGroupFile(filePath={})", filePath);
        GroupParser parser;
        try
        {
            ANTLRInputStream fs = new ANTLRInputStream(inputStream, charset.name());
            GroupLexer lexer = new GroupLexer(fs);
            fs.name = filePath.toString();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new GroupParser(tokens);
            parser.group(this, prefix);
        }
        catch (Exception e)
        {
            errMgr.ioError(null, ErrorType.CANT_LOAD_GROUP_FILE, e, filePath);
        }
    }

    protected void loadGroup(Path filePath, String sourceText, String prefix)
    {
        log.debug("loadGroup(filePath={})", filePath);
        try
        {
            ANTLRStringStream fs = new ANTLRStringStream(sourceText);
            GroupLexer lexer = new GroupLexer(fs);
            fs.name = filePath.toString();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GroupParser parser = new GroupParser(tokens);
            parser.group(this, prefix);
        }
        catch (RecognitionException e)
        {
            errMgr.ioError(null, ErrorType.CANT_LOAD_GROUP_FILE, e, filePath);
        }
    }

    @Override
    public void importTemplates(Token fileNameToken)
    {
        throw new UnsupportedOperationException();
    }
}
