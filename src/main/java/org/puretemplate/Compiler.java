package org.puretemplate;

import static java.util.Map.entry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.puretemplate.diagnostics.Instruction;
import org.puretemplate.error.ErrorType;
import org.puretemplate.exception.CompilationException;

/**
 * A compiler for a single template.
 */
class Compiler
{
    public static final String SUBTEMPLATE_PREFIX = "_sub";

    public static final int TEMPLATE_INITIAL_CODE_SIZE = 15;

    public static final Map<String, Interpreter.Option> supportedOptions = Map.ofEntries(entry("anchor",
            Interpreter.Option.ANCHOR),
        entry("format", Interpreter.Option.FORMAT),
        entry("null", Interpreter.Option.NULL),
        entry("separator", Interpreter.Option.SEPARATOR),
        entry("wrap", Interpreter.Option.WRAP));

    public static final int NUM_OPTIONS = supportedOptions.size();

    public static final Map<String, String> defaultOptionValues = Map.ofEntries(entry("anchor", "true"),
        entry("wrap", "\n"));

    public static final Map<String, Instruction> FUNCTIONS = createLookupMap(Instruction.FIRST,
        Instruction.LAST,
        Instruction.REST,
        Instruction.TRUNC,
        Instruction.STRIP,
        Instruction.TRIM,
        Instruction.LENGTH,
        Instruction.STRLEN,
        Instruction.REVERSE);

    private static Map<String, Instruction> createLookupMap(Instruction... instructions)
    {
        Map<String, Instruction> result = new HashMap<>();
        for (Instruction instruction : instructions)
        {
            result.put(instruction.formalName, instruction);
        }
        return Collections.unmodifiableMap(result);
    }

    private final STGroup group;

    public Compiler()
    {
        this(STGroup.defaultGroup);
    }

    public Compiler(STGroup group)
    {
        this.group = group;
    }

    public CompiledST compile(String template)
    {
        CompiledST code = compile(null, null, null, template, null);
        code.hasFormalArgs = false;
        return code;
    }

    /**
     * Compile full template with unknown formal arguments.
     */
    public CompiledST compile(String name, String template)
    {
        CompiledST code = compile(null, name, null, template, null);
        code.hasFormalArgs = false;
        return code;
    }

    /**
     * Compile full template with respect to a list of formal arguments.
     */
    public CompiledST compile(
        String sourceName, String name, List<FormalArgument> args, String template, Token templateToken)
    {
        ANTLRStringStream is = new ANTLRStringStream(template);
        is.name = sourceName != null
            ? sourceName
            : name;
        STLexer lexer;
        if (templateToken != null && templateToken.getType() == GroupParser.BIGSTRING_NO_NL)
        {
            lexer = new STLexer(group.errMgr, is, templateToken, group.delimiterStartChar, group.delimiterStopChar)
            {
                /** Throw out \n and indentation tokens inside BIGSTRING_NO_NL */
                @Override
                public Token nextToken()
                {
                    Token t = super.nextToken();
                    while (t.getType() == NEWLINE || t.getType() == INDENT)
                    {
                        t = super.nextToken();
                    }
                    return t;
                }
            };
        }
        else
        {
            lexer = new STLexer(group.errMgr, is, templateToken, group.delimiterStartChar, group.delimiterStopChar);
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        STParser p = new STParser(tokens, group.errMgr, templateToken);
        STParser.templateAndEOF_return r;
        try
        {
            r = p.templateAndEOF();
        }
        catch (RecognitionException re)
        {
            reportMessageAndThrowSTException(tokens, templateToken, p, re);
            return null;
        }
        if (p.getNumberOfSyntaxErrors() > 0 || r.getTree() == null)
        {
            CompiledST impl = new CompiledST();
            impl.defineFormalArgs(args);
            return impl;
        }

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(r.getTree());
        nodes.setTokenStream(tokens);
        CodeGenerator gen = new CodeGenerator(nodes, group.errMgr, name, template, templateToken);

        CompiledST impl = null;
        try
        {
            impl = gen.template(name, args);
            impl.nativeGroup = group;
            impl.template = template;
            impl.ast = r.getTree();
            impl.ast.setUnknownTokenBoundaries();
            impl.tokens = tokens;
        }
        catch (RecognitionException re)
        {
            group.errMgr.internalError(null, "bad tree structure", re);
        }

        return impl;
    }

    public static CompiledST defineBlankRegion(CompiledST outermostImpl, Token nameToken)
    {
        String outermostTemplateName = outermostImpl.name;
        String mangled = STGroup.getMangledRegionName(outermostTemplateName, nameToken.getText());
        CompiledST blank = new CompiledST();
        blank.isRegion = true;
        blank.templateDefStartToken = nameToken;
        blank.regionDefType = ST.RegionType.IMPLICIT;
        blank.name = mangled;
        outermostImpl.addImplicitlyDefinedTemplate(blank);
        return blank;
    }

    protected void reportMessageAndThrowSTException(
        TokenStream tokens, Token templateToken, Parser parser, RecognitionException re)
    {
        if (re.token.getType() == STLexer.EOF_TYPE)
        {
            String msg = "premature EOF";
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        else if (re instanceof NoViableAltException)
        {
            String msg = "'" + re.token.getText() + "' came as a complete surprise to me";
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        else if (tokens.index() == 0)
        {
            // couldn't parse anything
            String msg = "this doesn't look like a template: \"" + tokens + "\"";
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        else if (tokens.LA(1) == STLexer.LDELIM)
        {
            // couldn't parse expr
            String msg = "doesn't look like an expression";
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        else
        {
            String msg = parser.getErrorMessage(re, parser.getTokenNames());
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        throw new CompilationException(); // we have reported the error, so just blast out
    }
}
