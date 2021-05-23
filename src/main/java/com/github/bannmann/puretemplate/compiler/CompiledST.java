package com.github.bannmann.puretemplate.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import com.github.bannmann.puretemplate.Interpreter;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STGroup;
import com.github.bannmann.puretemplate.misc.Interval;
import com.github.bannmann.puretemplate.misc.Misc;

/**
 * The result of compiling an {@link ST}.  Contains all the bytecode instructions, string table, bytecode address to
 * source code map, and other bookkeeping info.  It's the implementation of an ST you might say.  All instances of the
 * same template share a single implementation ({@link ST#impl} field).
 */
public class CompiledST implements Cloneable
{
    public String name;

    /**
     * Every template knows where it is relative to the group that loaded it. The prefix is the relative path from the
     * root. {@code "/prefix/name"} is the fully qualified name of this template. All calls to {@link
     * STGroup#getInstanceOf} calls must use fully qualified names. A {@code "/"} is added to the front if you don't
     * specify one. Template references within template code, however, uses relative names, unless of course the name
     * starts with {@code "/"}.
     * <p>
     * This has nothing to do with the outer filesystem path to the group dir or group file.</p>
     * <p>
     * We set this as we load/compile the template.</p>
     * <p>
     * Always ends with {@code "/"}.</p>
     */
    public String prefix = "/";

    /**
     * The original, immutable pattern (not really used again after initial "compilation"). Useful for debugging.  Even
     * for subtemplates, this is entire overall template.
     */
    public String template;

    /**
     * The token that begins template definition; could be {@code <@r>} of region.
     */
    public Token templateDefStartToken;

    /**
     * Overall token stream for template (debug only).
     */
    public TokenStream tokens;

    /**
     * How do we interpret syntax of template? (debug only)
     */
    public CommonTree ast;

    public Map<String, FormalArgument> formalArguments;

    public boolean hasFormalArgs;

    public int numberOfArgsWithDefaultValues;

    /**
     * A list of all regions and subtemplates.
     */
    public List<CompiledST> implicitlyDefinedTemplates;

    /**
     * The group that physically defines this {@link ST} definition. We use it to initiate interpretation via {@link
     * ST#toString}. From there, it becomes field {@link Interpreter#group} and is fixed until rendering completes.
     */
    public STGroup nativeGroup = STGroup.defaultGroup;

    /**
     * Does this template come from a {@code <@region>...<@end>} embedded in another template?
     */
    public boolean isRegion;

    /**
     * If someone refs {@code <@r()>} in template t, an implicit
     *
     * <p>
     * {@code @t.r() ::= ""}</p>
     * <p>
     * is defined, but you can overwrite this def by defining your own. We need to prevent more than one manual def
     * though. Between this var and {@link #isRegion} we can determine these cases.</p>
     */
    public ST.RegionType regionDefType;

    public boolean isAnonSubtemplate; // {...}

    /**
     * string operands of instructions
     */
    public String[] strings;

    /**
     * byte-addressable code memory. For efficiency, this stores opcodes instead of references to the {@link
     * Bytecode.Instruction} enum.
     */
    public byte[] instrs;

    public int codeSize;

    /**
     * maps IP to range in template pattern
     */
    public Interval[] sourceMap;

    public CompiledST()
    {
        instrs = new byte[Compiler.TEMPLATE_INITIAL_CODE_SIZE];
        sourceMap = new Interval[Compiler.TEMPLATE_INITIAL_CODE_SIZE];
        template = "";
    }

    /**
     * Cloning the {@link CompiledST} for an {@link ST} instance allows {@link ST#add} to be called safely during
     * interpretation for templates that do not contain formal arguments.
     *
     * @return A copy of the current {@link CompiledST} instance. The copy is a shallow copy, with the exception of the
     * {@link #formalArguments} field which is also cloned.
     *
     * @throws CloneNotSupportedException If the current instance cannot be cloned.
     */
    @Override
    public CompiledST clone() throws CloneNotSupportedException
    {
        CompiledST clone = (CompiledST) super.clone();
        if (formalArguments != null)
        {
            formalArguments = Collections.synchronizedMap(new LinkedHashMap<String, FormalArgument>(formalArguments));
        }

        return clone;
    }

    public void addImplicitlyDefinedTemplate(CompiledST sub)
    {
        sub.prefix = this.prefix;
        if (sub.name.charAt(0) != '/')
        {
            sub.name = sub.prefix + sub.name;
        }
        if (implicitlyDefinedTemplates == null)
        {
            implicitlyDefinedTemplates = new ArrayList<CompiledST>();
        }
        implicitlyDefinedTemplates.add(sub);
    }

    public void defineArgDefaultValueTemplates(STGroup group)
    {
        if (formalArguments == null)
        {
            return;
        }
        for (String a : formalArguments.keySet())
        {
            FormalArgument fa = formalArguments.get(a);
            if (fa.defaultValueToken != null)
            {
                numberOfArgsWithDefaultValues++;
                switch (fa.defaultValueToken.getType())
                {
                    case GroupParser.ANONYMOUS_TEMPLATE:
                        String argSTname = fa.name + "_default_value";
                        Compiler c2 = new Compiler(group);
                        String defArgTemplate = Misc.strip(fa.defaultValueToken.getText(), 1);
                        fa.compiledDefaultValue = c2.compile(group.getFileName(),
                            argSTname,
                            null,
                            defArgTemplate,
                            fa.defaultValueToken);
                        fa.compiledDefaultValue.name = argSTname;
                        fa.compiledDefaultValue.defineImplicitlyDefinedTemplates(group);
                        break;

                    case GroupParser.STRING:
                        fa.defaultValue = Misc.strip(fa.defaultValueToken.getText(), 1);
                        break;

                    case GroupParser.LBRACK:
                        fa.defaultValue = Collections.emptyList();
                        break;

                    case GroupParser.TRUE:
                    case GroupParser.FALSE:
                        fa.defaultValue = fa.defaultValueToken.getType() == GroupParser.TRUE;
                        break;

                    default:
                        throw new UnsupportedOperationException("Unexpected default value token type.");
                }
            }
        }
    }

    public void defineFormalArgs(List<FormalArgument> args)
    {
        hasFormalArgs = true; // even if no args; it's formally defined
        if (args == null)
        {
            formalArguments = null;
        }
        else
        {
            for (FormalArgument a : args)
            {
                addArg(a);
            }
        }
    }

    /**
     * Used by {@link ST#add} to add args one by one without turning on full formal args definition signal.
     */
    public void addArg(FormalArgument a)
    {
        if (formalArguments == null)
        {
            formalArguments = Collections.synchronizedMap(new LinkedHashMap<String, FormalArgument>());
        }
        else if (formalArguments.containsKey(a.name))
        {
            throw new IllegalArgumentException(String.format("Formal argument %s already exists.", a.name));
        }

        a.index = formalArguments.size();
        formalArguments.put(a.name, a);
    }

    public void defineImplicitlyDefinedTemplates(STGroup group)
    {
        if (implicitlyDefinedTemplates != null)
        {
            for (CompiledST sub : implicitlyDefinedTemplates)
            {
                group.rawDefineTemplate(sub.name, sub, sub.templateDefStartToken);
                sub.defineImplicitlyDefinedTemplates(group);
            }
        }
    }

    public String getTemplateSource()
    {
        Interval r = getTemplateRange();
        return template.substring(r.a, r.b + 1);
    }

    public Interval getTemplateRange()
    {
        if (isAnonSubtemplate)
        {
            int start = Integer.MAX_VALUE;
            int stop = Integer.MIN_VALUE;
            for (Interval interval : sourceMap)
            {
                if (interval == null)
                {
                    continue;
                }

                start = Math.min(start, interval.a);
                stop = Math.max(stop, interval.b);
            }

            if (start <= stop + 1)
            {
                return new Interval(start, stop);
            }
        }
        return new Interval(0, template.length() - 1);
    }

    public String instrs()
    {
        BytecodeDisassembler dis = new BytecodeDisassembler(this);
        return dis.instrs();
    }

    public void dump()
    {
        BytecodeDisassembler dis = new BytecodeDisassembler(this);
        System.out.println(name + ":");
        System.out.println(dis.disassemble());
        System.out.println("Strings:");
        System.out.println(dis.strings());
        System.out.println("Bytecode to template map:");
        System.out.println(dis.sourceMap());
    }

    public String disasm()
    {
        BytecodeDisassembler dis = new BytecodeDisassembler(this);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(dis.disassemble());
        pw.println("Strings:");
        pw.println(dis.strings());
        pw.println("Bytecode to template map:");
        pw.println(dis.sourceMap());
        pw.close();
        return sw.toString();
    }
}
