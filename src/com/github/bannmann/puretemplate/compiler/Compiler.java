package com.github.bannmann.puretemplate.compiler;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import com.github.bannmann.puretemplate.Interpreter;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.misc.ErrorType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A compiler for a single template. */
public class Compiler {
    public static final String SUBTEMPLATE_PREFIX = "_sub";

    public static final int TEMPLATE_INITIAL_CODE_SIZE = 15;

    public static final Map<String, Interpreter.Option> supportedOptions;

    static {
        final Map<String, Interpreter.Option> map = new HashMap<String, Interpreter.Option>();
        map.put("anchor",    Interpreter.Option.ANCHOR);
        map.put("format",    Interpreter.Option.FORMAT);
        map.put("null",      Interpreter.Option.NULL);
        map.put("separator", Interpreter.Option.SEPARATOR);
        map.put("wrap",      Interpreter.Option.WRAP);
        supportedOptions = Collections.unmodifiableMap(map);
    }

    public static final int NUM_OPTIONS = supportedOptions.size();

    public static final Map<String,String> defaultOptionValues;

    static {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("anchor", "true");
        map.put("wrap",   "\n");
        defaultOptionValues = Collections.unmodifiableMap(map);
    }

    public static Map<String, Short> funcs;

    static {
        final Map<String, Short> map = new HashMap<String, Short>();
        map.put("first", Bytecode.INSTR_FIRST);
        map.put("last", Bytecode.INSTR_LAST);
        map.put("rest", Bytecode.INSTR_REST);
        map.put("trunc", Bytecode.INSTR_TRUNC);
        map.put("strip", Bytecode.INSTR_STRIP);
        map.put("trim", Bytecode.INSTR_TRIM);
        map.put("length", Bytecode.INSTR_LENGTH);
        map.put("strlen", Bytecode.INSTR_STRLEN);
        map.put("reverse", Bytecode.INSTR_REVERSE);
        funcs = Collections.unmodifiableMap(map);
    }

    /** Name subtemplates {@code _sub1}, {@code _sub2}, ... */
    public static AtomicInteger subtemplateCount = new AtomicInteger(0);

    public STGroup group;

    public Compiler() { this(STGroup.defaultGroup); }
    public Compiler(STGroup group) { this.group = group; }

    public CompiledST compile(String template) {
        CompiledST code = compile(null, null, null, template, null);
        code.hasFormalArgs = false;
        return code;
    }

    /** Compile full template with unknown formal arguments. */
    public CompiledST compile(String name, String template) {
        CompiledST code = compile(null, name, null, template, null);
        code.hasFormalArgs = false;
        return code;
    }

    /** Compile full template with respect to a list of formal arguments. */
    public CompiledST compile(String srcName,
                              String name,
                              List<FormalArgument> args,
                              String template,
                              Token templateToken)
    {
        ANTLRStringStream is = new ANTLRStringStream(template);
        is.name = srcName!=null ? srcName : name;
        STLexer lexer;
        if ( templateToken!=null &&
             templateToken.getType() == GroupParser.BIGSTRING_NO_NL )
        {
            lexer = new STLexer(group.errMgr, is, templateToken,
                        group.delimiterStartChar, group.delimiterStopChar) {
                /** Throw out \n and indentation tokens inside BIGSTRING_NO_NL */
                @Override
                public Token nextToken() {
                    Token t = super.nextToken();
                    while ( t.getType() == STLexer.NEWLINE ||
                            t.getType()==STLexer.INDENT )
                    {
                        t = super.nextToken();
                    }
                    return t;
                }
            };
        }
        else {
            lexer = new STLexer(group.errMgr, is, templateToken,
                                group.delimiterStartChar, group.delimiterStopChar);
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        STParser p = new STParser(tokens, group.errMgr, templateToken);
        STParser.templateAndEOF_return r;
        try {
            r = p.templateAndEOF();
        }
        catch (RecognitionException re) {
            reportMessageAndThrowSTException(tokens, templateToken, p, re);
            return null;
        }
        if ( p.getNumberOfSyntaxErrors()>0 || r.getTree()==null ) {
            CompiledST impl = new CompiledST();
            impl.defineFormalArgs(args);
            return impl;
        }

        //System.out.println(((CommonTree)r.getTree()).toStringTree());
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(r.getTree());
        nodes.setTokenStream(tokens);
        CodeGenerator gen = new CodeGenerator(nodes, group.errMgr, name, template, templateToken);

        CompiledST impl=null;
        try {
            impl = gen.template(name,args);
            impl.nativeGroup = group;
            impl.template = template;
            impl.ast = r.getTree();
            impl.ast.setUnknownTokenBoundaries();
            impl.tokens = tokens;
        }
        catch (RecognitionException re) {
            group.errMgr.internalError(null, "bad tree structure", re);
        }

        return impl;
    }

    public static CompiledST defineBlankRegion(CompiledST outermostImpl, Token nameToken) {
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

    public static String getNewSubtemplateName() {
        int count = subtemplateCount.incrementAndGet();
        return SUBTEMPLATE_PREFIX+count;
    }

    protected void reportMessageAndThrowSTException(TokenStream tokens, Token templateToken,
                                                    Parser parser, RecognitionException re)
    {
        if ( re.token.getType() == STLexer.EOF_TYPE ) {
            String msg = "premature EOF";
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        else if ( re instanceof NoViableAltException) {
            String msg = "'"+re.token.getText()+"' came as a complete surprise to me";
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        else if ( tokens.index() == 0 ) { // couldn't parse anything
            String msg = "this doesn't look like a template: \""+tokens+"\"";
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        else if ( tokens.LA(1) == STLexer.LDELIM ) { // couldn't parse expr
            String msg = "doesn't look like an expression";
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        else {
            String msg = parser.getErrorMessage(re, parser.getTokenNames());
            group.errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, re.token, msg);
        }
        throw new STException(); // we have reported the error, so just blast out
    }

}
