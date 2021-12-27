package org.puretemplate;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.puretemplate.diagnostics.ConstantReference;
import org.puretemplate.diagnostics.Instruction;
import org.puretemplate.diagnostics.Operand;
import org.puretemplate.diagnostics.OperandType;
import org.puretemplate.diagnostics.Statement;

import com.github.mizool.core.exception.CodeInconsistencyException;
import com.google.common.collect.ImmutableList;

/**
 * The result of compiling an {@link ST}.  Contains all the bytecode instructions, string table, bytecode address to
 * source code map, and other bookkeeping info.  It's the implementation of an ST you might say.  All instances of the
 * same template share a single implementation ({@link ST#impl} field).
 */
class CompiledST implements Cloneable
{
    String name;

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
    String prefix = "/";

    /**
     * The original, immutable pattern (not really used again after initial "compilation"). Useful for debugging.  Even
     * for subtemplates, this is entire overall template.
     */
    String template;

    /**
     * The token that begins template definition; could be {@code <@r>} of region.
     */
    Token templateDefStartToken;

    /**
     * Overall token stream for template (debug only).
     */
    TokenStream tokens;

    /**
     * How do we interpret syntax of template? (debug only)
     */
    CommonTree ast;

    Map<String, FormalArgument> formalArguments;

    boolean hasFormalArgs;

    int numberOfArgsWithDefaultValues;

    /**
     * A list of all regions and subtemplates.
     */
    private List<CompiledST> implicitlyDefinedTemplates;

    /**
     * The group that physically defines this {@link ST} definition. We use it to initiate interpretation via {@link
     * ST#toString}. From there, it becomes field {@code AbstractInterpreter.group} and is fixed until rendering
     * completes.
     */
    STGroup nativeGroup = STGroup.defaultGroup;

    /**
     * Does this template come from a {@code <@region>...<@end>} embedded in another template?
     */
    boolean isRegion;

    /**
     * If someone refs {@code <@r()>} in template t, an implicit
     *
     * <p>
     * {@code @t.r() ::= ""}</p>
     * <p>
     * is defined, but you can overwrite this def by defining your own. We need to prevent more than one manual def
     * though. Between this var and {@link #isRegion} we can determine these cases.</p>
     */
    ST.RegionType regionDefType;

    boolean isAnonSubtemplate;

    /**
     * string operands of instructions
     */
    String[] strings;

    /**
     * byte-addressable code memory. For efficiency, this stores opcodes instead of references to the {@link
     * Instruction} enum.
     */
    byte[] instrs;

    int codeSize;

    /**
     * maps IP to range in template pattern
     */
    Interval[] sourceMap;

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
            // FIXME should assign to clone.formalArguments, not our own!
            formalArguments = Collections.synchronizedMap(new LinkedHashMap<>(formalArguments));
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
            implicitlyDefinedTemplates = new ArrayList<>();
        }
        implicitlyDefinedTemplates.add(sub);
    }

    public void defineArgDefaultValueTemplates(STGroup group)
    {
        if (formalArguments == null)
        {
            return;
        }
        for (FormalArgument argument : formalArguments.values())
        {
            if (argument.defaultValueToken != null)
            {
                numberOfArgsWithDefaultValues++;
                switch (argument.defaultValueToken.getType())
                {
                    case GroupParser.ANONYMOUS_TEMPLATE:
                        String argSTname = argument.name + "_default_value";
                        Compiler c2 = new Compiler(group);
                        String defArgTemplate = Misc.strip(argument.defaultValueToken.getText(), 1);
                        argument.compiledDefaultValue = c2.compile(group.getFileName(),
                            argSTname,
                            null,
                            defArgTemplate,
                            argument.defaultValueToken);
                        argument.compiledDefaultValue.name = argSTname;
                        argument.compiledDefaultValue.defineImplicitlyDefinedTemplates(group);
                        break;

                    case GroupParser.STRING:
                        argument.defaultValue = Misc.strip(argument.defaultValueToken.getText(), 1);
                        break;

                    case GroupParser.LBRACK:
                        argument.defaultValue = Collections.emptyList();
                        break;

                    case GroupParser.TRUE:
                    case GroupParser.FALSE:
                        argument.defaultValue = argument.defaultValueToken.getType() == GroupParser.TRUE;
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
            formalArguments = Collections.synchronizedMap(new LinkedHashMap<>());
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

    public void dump(Consumer<String> printer)
    {
        printer.accept(name + ":");
        printer.accept(formatStatements());
        printer.accept("Strings:");
        printer.accept(formatStrings());
        printer.accept("Bytecode to template map:");
        printer.accept(formatSourceMap());
    }

    private String formatStatements()
    {
        StringBuilder buf = new StringBuilder();
        for (Statement statement : getStatements())
        {
            statement.appendTo(buf, Statement.Format.PRETTY);
            buf.append('\n');
        }
        return buf.toString();
    }

    public List<Statement> getStatements()
    {
        ImmutableList.Builder<Statement> result = ImmutableList.builder();
        int instructionPointer = 0;
        while (instructionPointer < codeSize)
        {
            Statement statement = createStatement(instructionPointer);
            result.add(statement);
            instructionPointer += statement.getSize();
        }
        return result.build();
    }

    public Statement createStatement(int instructionPointer)
    {
        if (instructionPointer >= codeSize)
        {
            throw new IllegalArgumentException("instructionPointer out of range: " + instructionPointer);
        }
        int startingInstructionPointer = instructionPointer;

        int opcode = instrs[instructionPointer];
        Instruction instruction = Bytecode.INSTRUCTIONS[opcode];
        if (instruction == null)
        {
            throw new IllegalArgumentException("no such instruction " + opcode + " at address " + instructionPointer);
        }
        instructionPointer++;

        ImmutableList.Builder<Operand> operands = ImmutableList.builder();
        for (OperandType operandType : instruction.operandTypes)
        {
            int opnd = Misc.getShort(instrs, instructionPointer);
            instructionPointer += Bytecode.OPND_SIZE_IN_BYTES;
            switch (operandType)
            {
                case STRING:
                    operands.add(OperandImpl.builder()
                        .type(OperandType.STRING)
                        .numericValue(opnd)
                        .stringConstant(getPoolString(opnd))
                        .build());
                    break;
                case ADDR:
                case INT:
                    operands.add(OperandImpl.builder()
                        .type(operandType)
                        .numericValue(opnd)
                        .build());
                    break;
                default:
                    throw new CodeInconsistencyException("unsupported operand type " + operandType);
            }
        }

        return StatementImpl.builder()
            .address(startingInstructionPointer)
            .instruction(instruction)
            .operands(operands.build())
            .size(instructionPointer - startingInstructionPointer)
            .build();
    }

    private ConstantReference getPoolString(int poolIndex)
    {
        if (poolIndex > strings.length)
        {
            return ConstantReferenceImpl.builder()
                .valid(false)
                .build();
        }

        return ConstantReferenceImpl.builder()
            .valid(true)
            .value(strings[poolIndex])
            .build();
    }

    private String formatStrings()
    {
        StringBuilder buf = new StringBuilder();
        int addr = 0;
        if (strings != null)
        {
            for (String s : strings)
            {
                if (s != null)
                {
                    s = Misc.replaceEscapes(s);
                    buf.append(String.format("%04d: \"%s\"\n", addr, s));
                }
                else
                {
                    buf.append(String.format("%04d: null\n", addr));
                }
                addr++;
            }
        }
        return buf.toString();
    }

    private String formatSourceMap()
    {
        StringBuilder buf = new StringBuilder();
        int addr = 0;
        for (Interval I : sourceMap)
        {
            if (I != null)
            {
                String chunk = template.substring(I.getA(), I.getB() + 1);
                buf.append(String.format("%04d: %s\t\"%s\"\n", addr, I, chunk));
            }
            addr++;
        }
        return buf.toString();
    }

    public String getDump()
    {
        try (StringBuilderWriter result = new StringBuilderWriter();
             PrintWriter printWriter = new PrintWriter(result))
        {
            dump(printWriter::println);
            return result.toString();
        }
    }

    public String getStatementsAsString()
    {
        return getStatements().stream()
            .map(statement -> statement.toString(Statement.Format.MINIMAL))
            .collect(Collectors.joining(", "));
    }
}
