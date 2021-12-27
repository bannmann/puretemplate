package org.puretemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.puretemplate.diagnostics.Event;
import org.puretemplate.diagnostics.Instruction;
import org.puretemplate.diagnostics.Statement;
import org.puretemplate.error.ErrorType;
import org.puretemplate.exception.NoSuchAttributeException;
import org.puretemplate.exception.NoSuchPropertyException;
import org.puretemplate.model.AttributeRenderer;
import org.puretemplate.model.ModelAdaptor;

import com.google.common.collect.Streams;

/**
 * This class knows how to execute template bytecodes relative to a particular {@link STGroup}. To execute the byte
 * codes, we need an output stream and a reference to an {@link ST} instance. That instance's {@link ST#getImpl() impl}
 * field points at a {@link CompiledST}, which contains all of the byte codes and other information relevant to
 * execution.
 * <p>
 * This interpreter is a stack-based bytecode interpreter. All operands go onto an operand stack.</p>
 * <p>
 * We create a new interpreter at the beginning of each rendering operation.</p>
 */
@Slf4j
abstract class AbstractInterpreter implements Interpreter
{
    private static class ObjectList extends ArrayList<Object>
    {
    }

    private static class ArgumentsMap extends HashMap<String, Object>
    {
    }

    private static final int DEFAULT_OPERAND_STACK_SIZE = 100;

    /**
     * Operand stack, grows upwards.
     */
    private final Object[] operands = new Object[DEFAULT_OPERAND_STACK_SIZE];

    /**
     * List-based access to {@link #operands}.
     */
    @SuppressWarnings("Java9CollectionFactory")
    private final List<Object> operandsList = Collections.unmodifiableList(Arrays.asList(operands));

    /**
     * Stack pointer register.
     */
    int stackPointer = -1;

    /**
     * The number of characters written on this template line so far.
     */
    int currentLineCharacters;

    /**
     * Render template with respect to this group.
     *
     * @see ST#groupThatCreatedThisInstance
     * @see CompiledST#nativeGroup
     */
    STGroup group;

    /**
     * For renderers, we have to pass in the locale.
     */
    Locale locale;

    ErrorManager errMgr;

    public AbstractInterpreter(STGroup group, Locale locale, ErrorManager errMgr)
    {
        this.group = group;
        this.locale = locale;
        this.errMgr = errMgr;
    }

    @Override
    public int exec(
        @NonNull ST template, @NonNull TemplateWriter templateWriter, @NonNull EventDistributor eventDistributor)
    {
        InstanceScope scope = new InstanceScope(null, template);
        Job job = new Job(templateWriter, eventDistributor);
        return exec(job, scope);
    }

    protected int exec(@NonNull Job job, @NonNull InstanceScope scope)
    {
        final ST self = scope.st;
        log.debug("exec({})", self.getName());
        try
        {
            setDefaultArguments(job, scope);
            return _exec(job, scope);
        }
        catch (Exception e)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            errMgr.runTimeError(scope.toLocation(), ErrorType.INTERNAL_ERROR, "internal error: " + sw);
            return 0;
        }
    }

    protected int _exec(Job job, InstanceScope scope)
    {
        final ST self = scope.st;
        TemplateWriter out = job.getTemplateWriter();
        int start = out.index(); // track char we're about to write
        Instruction prevOpcode = null;
        int n = 0; // how many char we write out
        int nargs;
        int nameIndex;
        int addr;
        String name;
        Object o, left, right;
        ST st;
        Object[] options;
        byte[] code = self.getImpl().instrs;        // which code block are we executing
        int ip = 0;
        while (ip < self.getImpl().codeSize)
        {
            trace(job, scope, ip);
            Instruction opcode = Bytecode.INSTRUCTIONS[code[ip]];
            scope.ip = ip;
            ip++; //jump to next instruction or first byte of operand
            switch (opcode)
            {
                case LOAD_STR:
                    // just testing...
                    load_str(self, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    break;
                case LOAD_ATTR:
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.getImpl().strings[nameIndex];
                    try
                    {
                        o = getAttribute(scope, name);
                        if (o == ST.EMPTY_ATTR)
                        {
                            o = null;
                        }
                    }
                    catch (NoSuchAttributeException nsae)
                    {
                        errMgr.runTimeError(scope.toLocation(), ErrorType.NO_SUCH_ATTRIBUTE, name);
                        o = null;
                    }
                    operands[++stackPointer] = o;
                    break;
                case LOAD_LOCAL:
                    int valueIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = self.locals[valueIndex];
                    if (o == ST.EMPTY_ATTR)
                    {
                        o = null;
                    }
                    operands[++stackPointer] = o;
                    break;
                case LOAD_PROP:
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = operands[stackPointer--];
                    name = self.getImpl().strings[nameIndex];
                    operands[++stackPointer] = getObjectProperty(job, scope, o, name);
                    break;
                case LOAD_PROP_IND:
                    Object propName = operands[stackPointer--];
                    o = operands[stackPointer];
                    operands[stackPointer] = getObjectProperty(job, scope, o, propName);
                    break;
                case NEW:
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.getImpl().strings[nameIndex];
                    nargs = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    // look up in original hierarchy not enclosing template (variable group)
                    // see TestSubtemplates.testEvalSTFromAnotherGroup()
                    st = self.groupThatCreatedThisInstance.getEmbeddedInstanceOf(scope, name);
                    // get n args and store into st's attr list
                    storeArgs(scope, nargs, st);
                    stackPointer -= nargs;
                    operands[++stackPointer] = st;
                    break;
                case NEW_IND:
                    nargs = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = (String) operands[stackPointer - nargs];
                    st = self.groupThatCreatedThisInstance.getEmbeddedInstanceOf(scope, name);
                    storeArgs(scope, nargs, st);
                    stackPointer -= nargs;
                    stackPointer--; // pop template name
                    operands[++stackPointer] = st;
                    break;
                case NEW_BOX_ARGS:
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.getImpl().strings[nameIndex];
                    Map<String, Object> attrs = (ArgumentsMap) operands[stackPointer--];
                    // look up in original hierarchy not enclosing template (variable group)
                    // see TestSubtemplates.testEvalSTFromAnotherGroup()
                    st = self.groupThatCreatedThisInstance.getEmbeddedInstanceOf(scope, name);
                    // get n args and store into st's attr list
                    storeArgs(scope, attrs, st);
                    operands[++stackPointer] = st;
                    break;
                case SUPER_NEW:
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.getImpl().strings[nameIndex];
                    nargs = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    super_new(scope, name, nargs);
                    break;
                case SUPER_NEW_BOX_ARGS:
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.getImpl().strings[nameIndex];
                    attrs = (ArgumentsMap) operands[stackPointer--];
                    super_new(scope, name, attrs);
                    break;
                case STORE_OPTION:
                    int optionIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = operands[stackPointer--];    // value to store
                    options = (Object[]) operands[stackPointer]; // get options
                    options[optionIndex] = o; // store value into options on stack
                    break;
                case STORE_ARG:
                    nameIndex = getShort(code, ip);
                    name = self.getImpl().strings[nameIndex];
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = operands[stackPointer--];
                    attrs = (ArgumentsMap) operands[stackPointer];
                    attrs.put(name, o); // leave attrs on stack
                    break;
                case WRITE:
                    o = operands[stackPointer--];
                    int n1 = writeObjectNoOptions(job, scope, o);
                    n += n1;
                    currentLineCharacters += n1;
                    break;
                case WRITE_OPT:
                    options = (Object[]) operands[stackPointer--]; // get options
                    o = operands[stackPointer--];                 // get option to write
                    int n2 = writeObjectWithOptions(job, scope, o, options);
                    n += n2;
                    currentLineCharacters += n2;
                    break;
                case MAP:
                    st = (ST) operands[stackPointer--]; // get prototype off stack
                    o = operands[stackPointer--];      // get object to map prototype across
                    map(scope, o, st);
                    break;
                case ROT_MAP:
                    int nmaps = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    List<ST> templates = new ArrayList<>();
                    for (int i = nmaps - 1; i >= 0; i--)
                    {
                        templates.add((ST) operands[stackPointer - i]);
                    }
                    stackPointer -= nmaps;
                    o = operands[stackPointer--];
                    if (o != null)
                    {
                        rot_map(scope, o, templates);
                    }
                    break;
                case ZIP_MAP:
                    st = (ST) operands[stackPointer--];
                    nmaps = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    List<Object> exprs = new ObjectList();
                    for (int i = nmaps - 1; i >= 0; i--)
                    {
                        exprs.add(operands[stackPointer - i]);
                    }
                    stackPointer -= nmaps;
                    operands[++stackPointer] = zip_map(scope, exprs, st);
                    break;
                case BR:
                    ip = getShort(code, ip);
                    break;
                case BRF:
                    addr = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = operands[stackPointer--]; // <if(expr)>...<endif>
                    if (!testAttributeTrue(o))
                    {
                        ip = addr; // jump
                    }
                    break;
                case OPTIONS:
                    operands[++stackPointer] = new Object[Compiler.NUM_OPTIONS];
                    break;
                case ARGS:
                    operands[++stackPointer] = new ArgumentsMap();
                    break;
                case PASSTHRU:
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.getImpl().strings[nameIndex];
                    attrs = (ArgumentsMap) operands[stackPointer];
                    passthru(scope, name, attrs);
                    break;
                case LIST:
                    operands[++stackPointer] = new ObjectList();
                    break;
                case ADD:
                    o = operands[stackPointer--];             // pop value
                    List<Object> list = (ObjectList) operands[stackPointer]; // don't pop list
                    addToList(list, o);
                    break;
                case TOSTR:
                    // replace with string value; early eval
                    operands[stackPointer] = toString(job, scope, operands[stackPointer]);
                    break;
                case FIRST:
                    operands[stackPointer] = first(operands[stackPointer]);
                    break;
                case LAST:
                    operands[stackPointer] = last(operands[stackPointer]);
                    break;
                case REST:
                    operands[stackPointer] = rest(operands[stackPointer]);
                    break;
                case TRUNC:
                    operands[stackPointer] = trunc(operands[stackPointer]);
                    break;
                case STRIP:
                    operands[stackPointer] = strip(operands[stackPointer]);
                    break;
                case TRIM:
                    o = operands[stackPointer--];
                    if (o.getClass() == String.class)
                    {
                        operands[++stackPointer] = ((String) o).trim();
                    }
                    else
                    {
                        errMgr.runTimeError(scope.toLocation(),
                            ErrorType.EXPECTING_STRING,
                            "trim",
                            o.getClass()
                                .getName());
                        operands[++stackPointer] = o;
                    }
                    break;
                case LENGTH:
                    operands[stackPointer] = length(operands[stackPointer]);
                    break;
                case STRLEN:
                    o = operands[stackPointer--];
                    if (o.getClass() == String.class)
                    {
                        operands[++stackPointer] = ((String) o).length();
                    }
                    else
                    {
                        errMgr.runTimeError(scope.toLocation(),
                            ErrorType.EXPECTING_STRING,
                            "strlen",
                            o.getClass()
                                .getName());
                        operands[++stackPointer] = 0;
                    }
                    break;
                case REVERSE:
                    operands[stackPointer] = reverse(operands[stackPointer]);
                    break;
                case NOT:
                    operands[stackPointer] = !testAttributeTrue(operands[stackPointer]);
                    break;
                case OR:
                    right = operands[stackPointer--];
                    left = operands[stackPointer--];
                    operands[++stackPointer] = testAttributeTrue(left) || testAttributeTrue(right);
                    break;
                case AND:
                    right = operands[stackPointer--];
                    left = operands[stackPointer--];
                    operands[++stackPointer] = testAttributeTrue(left) && testAttributeTrue(right);
                    break;
                case INDENT:
                    int strIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    indent(job, scope, strIndex);
                    break;
                case DEDENT:
                    out.popIndentation();
                    break;
                case NEWLINE:
                    try
                    {
                        if ((prevOpcode == null && !self.isAnonSubtemplate() && !self.getImpl().isRegion) ||
                            prevOpcode == Instruction.NEWLINE ||
                            prevOpcode == Instruction.INDENT ||
                            currentLineCharacters > 0)
                        {
                            out.write(Misc.NEWLINE);
                        }
                        currentLineCharacters = 0;
                    }
                    catch (IOException ioe)
                    {
                        errMgr.ioError(scope.toLocation(), ErrorType.WRITE_IO_ERROR, ioe);
                    }
                    break;
                case NOOP:
                    break;
                case POP:
                    stackPointer--; // throw away top of stack
                    break;
                case NULL:
                    operands[++stackPointer] = null;
                    break;
                case TRUE:
                    operands[++stackPointer] = true;
                    break;
                case FALSE:
                    operands[++stackPointer] = false;
                    break;
                case WRITE_STR:
                    strIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = self.getImpl().strings[strIndex];
                    n1 = writeText(job, scope, (String) o);
                    n += n1;
                    currentLineCharacters += n1;
                    break;
                default:
                    String dump = self.getImpl()
                        .getDump();
                    errMgr.internalError(scope.toLocation(),
                        MessageFormat.format("invalid bytecode @ {0}: {1}\n{2}", ip - 1, opcode, dump),
                        null);
            }
            prevOpcode = opcode;
        }
        fireEvent(job,
            () -> new EvalTemplateEvent(scope.toLocation(), start, out.index() - 1),
            ListenerInvoker.EVAL_TEMPLATE);
        return n;
    }

    void load_str(ST self, int ip)
    {
        int strIndex = getShort(self.getImpl().instrs, ip);
        operands[++stackPointer] = self.getImpl().strings[strIndex];
    }

    // TODO: refactor to remove dup'd code
    void super_new(InstanceScope scope, String name, int nargs)
    {
        final ST self = scope.st;
        ST st = null;
        CompiledST imported = self.getImpl().nativeGroup.lookupImportedTemplate(name);
        if (imported == null)
        {
            errMgr.runTimeError(scope.toLocation(), ErrorType.NO_IMPORTED_TEMPLATE, name);
            st = self.groupThatCreatedThisInstance.createStringTemplateInternally(new CompiledST());
        }
        else
        {
            st = imported.nativeGroup.getEmbeddedInstanceOf(scope, name);
            st.groupThatCreatedThisInstance = group;
        }
        // get n args and store into st's attr list
        storeArgs(scope, nargs, st);
        stackPointer -= nargs;
        operands[++stackPointer] = st;
    }

    void super_new(InstanceScope scope, String name, Map<String, Object> attrs)
    {
        final ST self = scope.st;
        ST st = null;
        CompiledST imported = self.getImpl().nativeGroup.lookupImportedTemplate(name);
        if (imported == null)
        {
            errMgr.runTimeError(scope.toLocation(), ErrorType.NO_IMPORTED_TEMPLATE, name);
            st = self.groupThatCreatedThisInstance.createStringTemplateInternally(new CompiledST());
        }
        else
        {
            st = imported.nativeGroup.createStringTemplateInternally(imported);
            st.groupThatCreatedThisInstance = group;
        }

        // get n args and store into st's attr list
        storeArgs(scope, attrs, st);
        operands[++stackPointer] = st;
    }

    void passthru(InstanceScope scope, String templateName, Map<String, Object> attrs)
    {
        CompiledST c = group.lookupTemplate(templateName);
        if (c == null)
        {
            return; // will get error later
        }
        if (c.formalArguments == null)
        {
            return;
        }
        for (FormalArgument arg : c.formalArguments.values())
        {
            // if not already set by user, set to value from outer scope
            if (!attrs.containsKey(arg.name))
            {
                try
                {
                    Object o = getAttribute(scope, arg.name);
                    // If the attribute exists but there is no value and
                    // the formal argument has no default value, make it null.
                    if (o == ST.EMPTY_ATTR && arg.defaultValueToken == null)
                    {
                        attrs.put(arg.name, null);
                    }
                    // Else, the attribute has an existing value, set arg.
                    else if (o != ST.EMPTY_ATTR)
                    {
                        attrs.put(arg.name, o);
                    }
                }
                catch (NoSuchAttributeException nsae)
                {
                    // if no such attribute exists for arg.name, set parameter
                    // if no default value
                    if (arg.defaultValueToken == null)
                    {
                        errMgr.runTimeError(scope.toLocation(), ErrorType.NO_SUCH_ATTRIBUTE_PASS_THROUGH, arg.name);
                        attrs.put(arg.name, null);
                    }
                }
            }
        }
    }

    void storeArgs(InstanceScope scope, Map<String, Object> attrs, ST st)
    {
        boolean noSuchAttributeReported = false;
        if (attrs != null)
        {
            for (Map.Entry<String, Object> argument : attrs.entrySet())
            {
                if (!st.getImpl().hasFormalArgs)
                {
                    if (st.getImpl().formalArguments == null ||
                        !st.getImpl().formalArguments.containsKey(argument.getKey()))
                    {
                        try
                        {
                            // we clone the CompiledST to prevent modifying the original
                            // formalArguments map during interpretation.
                            st.setImpl(st.getImpl()
                                .clone());
                            st.add(argument.getKey(), argument.getValue());
                        }
                        catch (CloneNotSupportedException ex)
                        {
                            noSuchAttributeReported = true;
                            errMgr.runTimeError(scope.toLocation(), ErrorType.NO_SUCH_ATTRIBUTE, argument.getKey());
                        }
                    }
                    else
                    {
                        st.rawSetAttribute(argument.getKey(), argument.getValue());
                    }
                }
                else
                {
                    // don't let it throw an exception in rawSetAttribute
                    if (st.getImpl().formalArguments == null ||
                        !st.getImpl().formalArguments.containsKey(argument.getKey()))
                    {
                        noSuchAttributeReported = true;
                        errMgr.runTimeError(scope.toLocation(), ErrorType.NO_SUCH_ATTRIBUTE, argument.getKey());
                        continue;
                    }

                    st.rawSetAttribute(argument.getKey(), argument.getValue());
                }
            }
        }

        if (st.getImpl().hasFormalArgs)
        {
            boolean argumentCountMismatch = false;
            Map<String, FormalArgument> formalArguments = st.getImpl().formalArguments;
            if (formalArguments == null)
            {
                formalArguments = Collections.emptyMap();
            }

            // first make sure that all non-default arguments are specified
            // ignore this check if a NO_SUCH_ATTRIBUTE error already occurred
            if (!noSuchAttributeReported)
            {
                for (Map.Entry<String, FormalArgument> formalArgument : formalArguments.entrySet())
                {
                    if (formalArgument.getValue().defaultValueToken != null ||
                        formalArgument.getValue().defaultValue != null)
                    {
                        // this argument has a default value, so it doesn't need to appear in attrs
                        continue;
                    }

                    if (attrs == null || !attrs.containsKey(formalArgument.getKey()))
                    {
                        argumentCountMismatch = true;
                        break;
                    }
                }
            }

            // next make sure there aren't too many arguments. note that the names
            // of arguments are checked below as they are applied to the template
            // instance, so there's no need to do that here.
            if (attrs != null && attrs.size() > formalArguments.size())
            {
                argumentCountMismatch = true;
            }

            if (argumentCountMismatch)
            {
                int nargs = attrs != null
                    ? attrs.size()
                    : 0;
                int nformalArgs = formalArguments.size();
                errMgr.runTimeError(scope.toLocation(),
                    ErrorType.ARGUMENT_COUNT_MISMATCH,
                    nargs,
                    st.getImpl().name,
                    nformalArgs);
            }
        }
    }

    void storeArgs(InstanceScope scope, int nargs, ST st)
    {
        if (nargs > 0 && !st.getImpl().hasFormalArgs && st.getImpl().formalArguments == null)
        {
            st.add(ST.IMPLICIT_ARG_NAME, null); // pretend we have "it" arg
        }

        int nformalArgs = 0;
        if (st.getImpl().formalArguments != null)
        {
            nformalArgs = st.getImpl().formalArguments.size();
        }
        int firstArg = stackPointer - (nargs - 1);
        int numToStore = Math.min(nargs, nformalArgs);
        if (st.getImpl().isAnonSubtemplate)
        {
            nformalArgs -= Language.PREDEFINED_ANON_SUBTEMPLATE_ATTRIBUTES.size();
        }

        if (nargs < (nformalArgs - st.getImpl().numberOfArgsWithDefaultValues) || nargs > nformalArgs)
        {
            errMgr.runTimeError(scope.toLocation(),
                ErrorType.ARGUMENT_COUNT_MISMATCH,
                nargs,
                st.getImpl().name,
                nformalArgs);
        }

        if (st.getImpl().formalArguments == null)
        {
            return;
        }

        Iterator<String> argNames = st.getImpl().formalArguments.keySet()
            .iterator();
        for (int i = 0; i < numToStore; i++)
        {
            Object o = operands[firstArg + i];    // value to store
            String argName = argNames.next();
            st.rawSetAttribute(argName, o);
        }
    }

    protected void indent(Job job, InstanceScope scope, int strIndex)
    {
        TemplateWriter out = job.getTemplateWriter();
        String indent = scope.st.getImpl().strings[strIndex];
        fireEvent(job, () -> {
            int start = out.index(); // track char we're about to write
            return new IndentEvent(scope.toLocation(),
                start,
                start + indent.length() - 1,
                getExprStartChar(scope),
                getExprStopChar(scope));
        }, ListenerInvoker.INDENT);
        out.pushIndentation(indent);
    }

    /**
     * Write out an expression result that doesn't use expression options. E.g., {@code <name>}
     */
    protected int writeObjectNoOptions(Job job, InstanceScope scope, Object o)
    {
        TemplateWriter out = job.getTemplateWriter();
        int start = out.index(); // track char we're about to write
        int n = writeObject(job, scope, o, null);
        fireEvent(job,
            () -> new EvalExpressionEvent(scope.toLocation(),
                start,
                out.index() - 1,
                getExprStartChar(scope),
                getExprStopChar(scope)),
            ListenerInvoker.EVAL_EXPRESSION);

        return n;
    }

    /**
     * Write out a text element, i.e. a part of the template that is neither expression nor comment
     */
    protected abstract int writeText(Job out, InstanceScope scope, String o);

    /**
     * Write out an expression result that uses expression options. E.g., {@code <names; separator=", ">}
     */
    protected int writeObjectWithOptions(Job job, InstanceScope scope, Object o, Object[] options)
    {
        TemplateWriter out = job.getTemplateWriter();
        int start = out.index(); // track char we're about to write
        // precompute all option values (render all the way to strings)
        String[] optionStrings = null;
        if (options != null)
        {
            optionStrings = new String[options.length];
            for (int i = 0; i < Compiler.NUM_OPTIONS; i++)
            {
                optionStrings[i] = toString(job, scope, options[i]);
            }
        }
        if (options != null && options[Option.ANCHOR.ordinal()] != null)
        {
            out.pushAnchorPoint();
        }

        int n = writeObject(job, scope, o, optionStrings);

        if (options != null && options[Option.ANCHOR.ordinal()] != null)
        {
            out.popAnchorPoint();
        }
        fireEvent(job,
            () -> new EvalExpressionEvent(scope.toLocation(),
                start,
                out.index() - 1,
                getExprStartChar(scope),
                getExprStopChar(scope)),
            ListenerInvoker.EVAL_EXPRESSION);
        return n;
    }

    /**
     * Generic method to emit text for an object. It differentiates between contexts/templates, iterable objects, and
     * plain old Java objects (POJOs)
     */
    protected int writeObject(Job job, InstanceScope scope, Object o, String[] options)
    {
        if (o == null)
        {
            if (options != null && options[Option.NULL.ordinal()] != null)
            {
                o = options[Option.NULL.ordinal()];
            }
            else
            {
                return 0;
            }
        }

        if (o instanceof ContextImpl)
        {
            o = ((ContextImpl) o).getSt();
        }

        int n = 0;
        if (o instanceof ST)
        {
            scope = new InstanceScope(scope, (ST) o);
            if (options != null && options[Option.WRAP.ordinal()] != null)
            {
                // if we have a wrap string, then inform writer it
                // might need to wrap
                try
                {
                    job.getTemplateWriter()
                        .writeWrap(options[Option.WRAP.ordinal()]);
                }
                catch (IOException ioe)
                {
                    errMgr.ioError(scope.toLocation(), ErrorType.WRITE_IO_ERROR, ioe);
                }
            }
            n = exec(job, scope);
        }
        else
        {
            o = convertAnythingIteratableToIterator(o); // normalize
            try
            {
                if (o instanceof Iterator)
                {
                    n = writeIterator(job, scope, o, options);
                }
                else
                {
                    n = writePOJO(job, scope, o, options);
                }
            }
            catch (IOException ioe)
            {
                errMgr.ioError(scope.toLocation(), ErrorType.WRITE_IO_ERROR, ioe, o);
            }
        }
        return n;
    }

    protected int writeIterator(Job job, InstanceScope scope, Object o, String[] options) throws IOException
    {
        if (o == null)
        {
            return 0;
        }
        int n = 0;
        Iterator<?> it = (Iterator<?>) o;
        String separator = null;
        if (options != null)
        {
            separator = options[Option.SEPARATOR.ordinal()];
        }
        boolean seenAValue = false;
        while (it.hasNext())
        {
            Object iterValue = it.next();
            // Emit separator if we're beyond first value
            boolean needSeparator = seenAValue && separator != null &&            // we have a separator and
                (
                    iterValue != null ||           // either we have a value
                        options[Option.NULL.ordinal()] != null); // or no value but null option
            if (needSeparator)
            {
                n += job.getTemplateWriter()
                    .writeSeparator(separator);
            }
            int nw = writeObject(job, scope, iterValue, options);
            if (nw > 0)
            {
                seenAValue = true;
            }
            n += nw;
        }
        return n;
    }

    protected int writePOJO(Job job, InstanceScope scope, Object o, String[] options) throws IOException
    {
        String formatString = null;
        if (options != null)
        {
            formatString = options[Option.FORMAT.ordinal()];
        }
        String v = renderObject(scope, formatString, o, o.getClass());
        int n;
        if (options != null && options[Option.WRAP.ordinal()] != null)
        {
            n = job.getTemplateWriter()
                .write(v, options[Option.WRAP.ordinal()]);
        }
        else
        {
            n = job.getTemplateWriter()
                .write(v);
        }
        return n;
    }

    private <T> String renderObject(InstanceScope scope, String formatString, Object o, Class<T> attributeType)
    {
        // ask the native group defining the surrounding template for the renderer
        AttributeRenderer<? super T> r = scope.st.getImpl().nativeGroup.getAttributeRenderer(attributeType);
        if (r != null)
        {
            return r.render(attributeType.cast(o), formatString, locale);
        }
        else
        {
            return o.toString();
        }
    }

    protected int getExprStartChar(InstanceScope scope)
    {
        Interval templateLocation = scope.st.getImpl().sourceMap[scope.ip];
        if (templateLocation != null)
        {
            return templateLocation.getA();
        }
        return -1;
    }

    protected int getExprStopChar(InstanceScope scope)
    {
        Interval templateLocation = scope.st.getImpl().sourceMap[scope.ip];
        if (templateLocation != null)
        {
            return templateLocation.getB();
        }
        return -1;
    }

    protected void map(InstanceScope scope, Object attr, final ST st)
    {
        rot_map(scope, attr, Collections.singletonList(st));
    }

    /**
     * Renders expressions of the form {@code <names:a()>} or {@code <names:a(),b()>}.
     */
    protected void rot_map(InstanceScope scope, Object attr, List<ST> prototypes)
    {
        if (attr == null)
        {
            operands[++stackPointer] = null;
            return;
        }
        attr = convertAnythingIteratableToIterator(attr);
        if (attr instanceof Iterator<?>)
        {
            List<ST> mapped = rot_map_iterator(scope, (Iterator<?>) attr, prototypes);
            operands[++stackPointer] = mapped;
        }
        else
        {
            // if only single value, just apply first template to sole value
            ST proto = prototypes.get(0);
            ST st = group.createStringTemplateInternally(proto);
            if (st != null)
            {
                setFirstArgument(scope, st, attr);
                if (st.getImpl().isAnonSubtemplate)
                {
                    st.rawSetAttribute("i0", 0);
                    st.rawSetAttribute("i", 1);
                }
                operands[++stackPointer] = st;
            }
            else
            {
                operands[++stackPointer] = null;
            }
        }
    }

    protected List<ST> rot_map_iterator(InstanceScope scope, Iterator<?> iter, List<ST> prototypes)
    {
        List<ST> mapped = new ArrayList<>();
        int i0 = 0;
        int i = 1;
        int ti = 0;
        while (iter.hasNext())
        {
            Object iterValue = iter.next();
            if (iterValue == null)
            {
                mapped.add(null);
                continue;
            }
            int templateIndex = ti % prototypes.size(); // rotate through
            ti++;
            ST proto = prototypes.get(templateIndex);
            ST st = group.createStringTemplateInternally(proto);
            setFirstArgument(scope, st, iterValue);
            if (st.getImpl().isAnonSubtemplate)
            {
                st.rawSetAttribute("i0", i0);
                st.rawSetAttribute("i", i);
            }
            mapped.add(st);
            i0++;
            i++;
        }
        return mapped;
    }

    /**
     * Renders expressions of the form {@code <names,phones:{n,p | ...}>} or {@code <a,b:t()>}.
     */
    // todo: i, i0 not set unless mentioned? map:{k,v | ..}?
    protected ST.AttributeList zip_map(InstanceScope scope, List<Object> exprs, ST prototype)
    {
        if (exprs == null || prototype == null || exprs.size() == 0)
        {
            return null; // do not apply if missing templates or empty values
        }
        // make everything iterable
        for (int i = 0; i < exprs.size(); i++)
        {
            Object attr = exprs.get(i);
            if (attr != null)
            {
                exprs.set(i, convertAnythingToIterator(attr));
            }
        }

        // ensure arguments line up
        int numExprs = exprs.size();
        CompiledST code = prototype.getImpl();
        Map<String, FormalArgument> formalArguments = code.formalArguments;
        if (!code.hasFormalArgs || formalArguments == null)
        {
            errMgr.runTimeError(scope.toLocation(), ErrorType.MISSING_FORMAL_ARGUMENTS);
            return null;
        }

        // todo: track formal args not names for efficient filling of locals
        String[] formalArgumentNames = formalArguments.keySet()
            .toArray(new String[0]);
        int nformalArgs = formalArgumentNames.length;
        if (prototype.isAnonSubtemplate())
        {
            nformalArgs -= Language.PREDEFINED_ANON_SUBTEMPLATE_ATTRIBUTES.size();
        }
        if (nformalArgs != numExprs)
        {
            errMgr.runTimeError(scope.toLocation(), ErrorType.MAP_ARGUMENT_COUNT_MISMATCH, numExprs, nformalArgs);
            // TODO just fill first n
            // truncate arg list to match smaller size
            int shorterSize = Math.min(formalArgumentNames.length, numExprs);
            numExprs = shorterSize;
            String[] newFormalArgumentNames = new String[shorterSize];
            System.arraycopy(formalArgumentNames, 0, newFormalArgumentNames, 0, shorterSize);
            formalArgumentNames = newFormalArgumentNames;
        }

        // keep walking while at least one attribute has values

        ST.AttributeList results = new ST.AttributeList();
        int i = 0; // iteration number from 0
        while (true)
        {
            // get a value for each attribute in list; put into ST instance
            int numEmpty = 0;
            ST embedded = group.createStringTemplateInternally(prototype);
            embedded.rawSetAttribute("i0", i);
            embedded.rawSetAttribute("i", i + 1);
            for (int a = 0; a < numExprs; a++)
            {
                Iterator<?> it = (Iterator<?>) exprs.get(a);
                if (it != null && it.hasNext())
                {
                    String argName = formalArgumentNames[a];
                    Object iteratedValue = it.next();
                    embedded.rawSetAttribute(argName, iteratedValue);
                }
                else
                {
                    numEmpty++;
                }
            }
            if (numEmpty == numExprs)
            {
                break;
            }
            results.add(embedded);
            i++;
        }
        return results;
    }

    protected void setFirstArgument(InstanceScope scope, ST st, Object attr)
    {
        if (!st.getImpl().hasFormalArgs)
        {
            if (st.getImpl().formalArguments == null)
            {
                st.add(ST.IMPLICIT_ARG_NAME, attr);
                return;
            }
            // else fall thru to set locals[0]
        }
        if (st.getImpl().formalArguments == null)
        {
            errMgr.runTimeError(scope.toLocation(), ErrorType.ARGUMENT_COUNT_MISMATCH, 1, st.getImpl().name, 0);
            return;
        }
        st.locals[0] = attr;
    }

    protected void addToList(List<Object> list, Object o)
    {
        o = convertAnythingIteratableToIterator(o);
        if (o instanceof Iterator)
        {
            // copy of elements into our temp list
            Iterator<?> it = (Iterator<?>) o;
            while (it.hasNext())
            {
                list.add(it.next());
            }
        }
        else
        {
            list.add(o);
        }
    }

    /**
     * Return the first attribute if multi-valued, or the attribute itself if single-valued.
     * <p>
     * This method is used for rendering expressions of the form {@code <names:first()>}.</p>
     */
    public Object first(Object v)
    {
        if (v == null)
        {
            return null;
        }
        Object r = v;
        v = convertAnythingIteratableToIterator(v);
        if (v instanceof Iterator)
        {
            Iterator<?> it = (Iterator<?>) v;
            if (it.hasNext())
            {
                r = it.next();
            }
        }
        return r;
    }

    /**
     * Return the last attribute if multi-valued, or the attribute itself if single-valued. Unless it's a {@link List}
     * or array, this is pretty slow as it iterates until the last element.
     * <p>
     * This method is used for rendering expressions of the form {@code <names:last()>}.</p>
     */
    public Object last(Object v)
    {
        if (v == null)
        {
            return null;
        }
        if (v instanceof List)
        {
            return ((List<?>) v).get(((List<?>) v).size() - 1);
        }
        else if (v.getClass()
            .isArray())
        {
            return Array.get(v, Array.getLength(v) - 1);
        }
        Object last = v;
        v = convertAnythingIteratableToIterator(v);
        if (v instanceof Iterator)
        {
            Iterator<?> it = (Iterator<?>) v;
            while (it.hasNext())
            {
                last = it.next();
            }
        }
        return last;
    }

    /**
     * Return everything but the first attribute if multi-valued, or {@code null} if single-valued.
     */
    public Object rest(Object v)
    {
        if (v == null)
        {
            return null;
        }
        if (v instanceof List)
        {
            // optimize list case
            List<?> elems = (List<?>) v;
            if (elems.size() <= 1)
            {
                return null;
            }
            return elems.subList(1, elems.size());
        }
        v = convertAnythingIteratableToIterator(v);
        if (v instanceof Iterator)
        {
            List<Object> a = new ArrayList<>();
            Iterator<?> it = (Iterator<?>) v;
            if (!it.hasNext())
            {
                return null; // if not even one value return null
            }
            it.next(); // ignore first value
            while (it.hasNext())
            {
                Object o = it.next();
                a.add(o);
            }
            return a;
        }
        return null;  // rest of single-valued attribute is null
    }

    /**
     * Return all but the last element. <code>trunc(<i>x</i>)==null</code> if <code><i>x</i></code> is single-valued.
     */
    public Object trunc(Object v)
    {
        if (v == null)
        {
            return null;
        }
        if (v instanceof List)
        {
            // optimize list case
            List<?> elems = (List<?>) v;
            if (elems.size() <= 1)
            {
                return null;
            }
            return elems.subList(0, elems.size() - 1);
        }
        v = convertAnythingIteratableToIterator(v);
        if (v instanceof Iterator)
        {
            List<Object> a = new ArrayList<>();
            Iterator<?> it = (Iterator<?>) v;
            while (it.hasNext())
            {
                Object o = it.next();
                if (it.hasNext())
                {
                    a.add(o); // only add if not last one
                }
            }
            return a;
        }
        return null; // trunc(x)==null when x single-valued attribute
    }

    /**
     * Return a new list without {@code null} values.
     */
    public Object strip(Object v)
    {
        if (v == null)
        {
            return null;
        }
        v = convertAnythingIteratableToIterator(v);
        if (v instanceof Iterator)
        {
            List<Object> a = new ArrayList<>();
            Iterator<?> it = (Iterator<?>) v;
            while (it.hasNext())
            {
                Object o = it.next();
                if (o != null)
                {
                    a.add(o);
                }
            }
            return a;
        }
        return v; // strip(x)==x when x single-valued attribute
    }

    /**
     * Return a list with the same elements as {@code v} but in reverse order.
     * <p>
     * Note that {@code null} values are <i>not</i> stripped out; use {@code reverse(strip(v))} to do that.</p>
     */
    public Object reverse(Object v)
    {
        if (v == null)
        {
            return null;
        }
        v = convertAnythingIteratableToIterator(v);
        if (v instanceof Iterator)
        {
            List<Object> a = new LinkedList<>();
            Iterator<?> it = (Iterator<?>) v;
            while (it.hasNext())
            {
                a.add(0, it.next());
            }
            return a;
        }
        return v;
    }

    /**
     * Return the length of a multi-valued attribute or 1 if it is a single attribute. If {@code v} is {@code null}
     * return 0.
     * <p>
     * The implementation treats several common collections and arrays as special cases for speed.</p>
     */
    public Object length(Object v)
    {
        if (v == null)
        {
            return 0;
        }
        int i = 1;      // we have at least one of something. Iterator and arrays might be empty.
        if (v instanceof Map)
        {
            i = ((Map<?, ?>) v).size();
        }
        else if (v instanceof Collection)
        {
            i = ((Collection<?>) v).size();
        }
        else if (v instanceof Object[])
        {
            i = ((Object[]) v).length;
        }
        else if (v.getClass()
            .isArray())
        {
            i = Array.getLength(v);
        }
        else if (v instanceof Iterable || v instanceof Iterator)
        {
            Iterator<?> it = v instanceof Iterable
                ? ((Iterable<?>) v).iterator()
                : (Iterator<?>) v;
            i = 0;
            while (it.hasNext())
            {
                it.next();
                i++;
            }
        }
        return i;
    }

    protected String toString(Job job, InstanceScope scope, Object value)
    {
        if (value != null)
        {
            if (value instanceof String)
            {
                return (String) value;
            }

            // if not string already, must evaluate it
            return evaluateObject(job, scope, value);
        }
        return null;
    }

    private String evaluateObject(Job job, InstanceScope scope, Object value)
    {
        StringWriter result = new StringWriter();
        TemplateWriter stw = job.getTemplateWriter()
            .createWriterTargeting(result);
        Job subJob = job.withTemplateWriter(stw);

        // TODO this behavior does not affect any unit test. find out why it exists and add one.
        if (!scope.earlyEval)
        {
            scope = new InstanceScope(scope, scope.st);
            scope.earlyEval = true;
        }

        writeObjectNoOptions(subJob, scope, value);
        return result.toString();
    }

    public static Object convertAnythingIteratableToIterator(Object o)
    {
        Iterator<?> iter = null;
        if (o == null)
        {
            return null;
        }
        if (o instanceof Iterable)
        {
            iter = ((Iterable<?>) o).iterator();
        }
        else if (o instanceof Object[])
        {
            iter = Arrays.asList((Object[]) o)
                .iterator();
        }
        else if (o.getClass()
            .isArray())
        {
            iter = new ArrayIterator(o);
        }
        else if (o instanceof Map)
        {
            iter = ((Map<?, ?>) o).keySet()
                .iterator();
        }
        if (iter == null)
        {
            return o;
        }
        return iter;
    }

    public Iterator<?> convertAnythingToIterator(Object o)
    {
        o = convertAnythingIteratableToIterator(o);
        if (o instanceof Iterator)
        {
            return (Iterator<?>) o;
        }
        List<Object> singleton = new ST.AttributeList(1);
        singleton.add(o);
        return singleton.iterator();
    }

    protected boolean testAttributeTrue(Object a)
    {
        if (a == null)
        {
            return false;
        }
        if (a instanceof Boolean)
        {
            return (Boolean) a;
        }
        if (a instanceof Collection)
        {
            return ((Collection<?>) a).size() > 0;
        }
        if (a instanceof Map)
        {
            return ((Map<?, ?>) a).size() > 0;
        }
        if (a instanceof Iterable)
        {
            return ((Iterable<?>) a).iterator()
                .hasNext();
        }
        if (a instanceof Iterator)
        {
            return ((Iterator<?>) a).hasNext();
        }
        return true; // any other non-null object, return true--it's present
    }

    protected Object getObjectProperty(Job job, InstanceScope scope, Object o, Object property)
    {
        if (o == null)
        {
            errMgr.runTimeError(scope.toLocation(), ErrorType.NO_SUCH_PROPERTY, "null." + property);
            return null;
        }

        try
        {
            ModelAdaptor adap = scope.st.groupThatCreatedThisInstance.getModelAdaptor(o.getClass());
            return adap.getProperty(o, property, toString(job, scope, property));
        }
        catch (NoSuchPropertyException e)
        {
            errMgr.runTimeError(scope.toLocation(),
                ErrorType.NO_SUCH_PROPERTY,
                e,
                o.getClass()
                    .getName() + "." + property);
        }
        return null;
    }

    /**
     * Find an attribute via dynamic scoping up enclosing scope chain. Only look for a dictionary definition if the
     * attribute is not found, so attributes sent in to a template override dictionary names.
     * <p>
     * Return {@link ST#EMPTY_ATTR} if found definition but no value.</p>
     */
    public Object getAttribute(@NonNull InstanceScope scope, String name)
    {
        InstanceScope current = scope;
        while (current != null)
        {
            ST p = current.st;
            FormalArgument localArg = null;
            if (p.getImpl().formalArguments != null)
            {
                localArg = p.getImpl().formalArguments.get(name);
            }
            if (localArg != null)
            {
                return p.locals[localArg.index];
            }
            current = current.parent; // look up enclosing scope chain
        }
        // got to root scope and no definition, try dictionaries in group and up
        final ST self = scope.st;
        STGroup g = self.getImpl().nativeGroup;
        Object o = getDictionary(g, name);
        if (o != null)
        {
            return o;
        }

        // not found, report unknown attr
        throw new NoSuchAttributeException(name, scope.toLocation());
    }

    public Object getDictionary(STGroup g, String name)
    {
        if (g.isDictionary(name))
        {
            return g.rawGetDictionary(name);
        }
        for (STGroup sup : g.imports)
        {
            Object o = getDictionary(sup, name);
            if (o != null)
            {
                return o;
            }
        }
        return null;
    }

    /**
     * Set any default argument values that were not set by the invoking template or by {@link ST#add} directly. Note
     * that the default values may be templates.
     * <p>
     * The evaluation context is the {@code invokedST} template itself so template default arguments can see other
     * arguments.</p>
     */
    public void setDefaultArguments(Job job, InstanceScope scope)
    {
        final ST invokedST = scope.st;
        if (invokedST.getImpl().formalArguments == null || invokedST.getImpl().numberOfArgsWithDefaultValues == 0)
        {
            return;
        }
        for (FormalArgument arg : invokedST.getImpl().formalArguments.values())
        {
            // if no value for attribute and default arg, inject default arg into self
            if (invokedST.locals[arg.index] != ST.EMPTY_ATTR || arg.defaultValueToken == null)
            {
                continue;
            }
            if (arg.defaultValueToken.getType() == GroupParser.ANONYMOUS_TEMPLATE)
            {
                CompiledST code = arg.compiledDefaultValue;
                if (code == null)
                {
                    code = new CompiledST();
                }
                ST defaultArgST = group.createStringTemplateInternally(code);
                defaultArgST.groupThatCreatedThisInstance = group;
                // If default arg is template with single expression
                // wrapped in parens, x={<(...)>}, then eval to string
                // rather than setting x to the template for later
                // eval.
                String defArgTemplate = arg.defaultValueToken.getText();
                if (defArgTemplate.startsWith("{" + group.delimiterStartChar + "(") &&
                    defArgTemplate.endsWith(")" + group.delimiterStopChar + "}"))
                {

                    invokedST.rawSetAttribute(arg.name,
                        toString(job, new InstanceScope(scope, invokedST), defaultArgST));
                }
                else
                {
                    invokedST.rawSetAttribute(arg.name, defaultArgST);
                }
            }
            else
            {
                invokedST.rawSetAttribute(arg.name, arg.defaultValue);
            }
        }
    }

    protected void trace(Job job, InstanceScope scope, int ip)
    {
        fireEvent(job,
            () -> TraceEvent.builder()
                .statement(getTraceStatement(scope, ip))
                .stack(getTraceStack(operandsList, stackPointer))
                .location(scope.toLocation())
                .stackPointer(stackPointer)
                .currentLineCharacters(currentLineCharacters)
                .build(),
            ListenerInvoker.TRACE);
    }

    private static Statement getTraceStatement(InstanceScope scope, int ip)
    {
        return scope.st.getImpl()
            .createStatement(ip);
    }

    private List<String> getTraceStack(List<Object> operandsList, int stackPointer)
    {
        return operandsList.stream()
            .limit(stackPointer + 1L)
            .map(this::getTraceStackElement)
            .collect(Collectors.toList());
    }

    protected String getTraceStackElement(Object o)
    {
        if (o instanceof ST)
        {
            if (((ST) o).getImpl() == null)
            {
                return "bad-template()";
            }
            else
            {
                return String.format("%s()", ((ST) o).getImpl().name);
            }
        }
        o = convertAnythingIteratableToIterator(o);
        if (o instanceof Iterator)
        {
            Iterator<?> it = (Iterator<?>) o;
            return Streams.stream(it)
                .map(this::getTraceStackElement)
                .collect(Collectors.joining(" ", "[", "]"));
        }
        else
        {
            return String.valueOf(o);
        }
    }

    public <D extends Distributable<E>, E extends Event & Event.DistributionTarget> void fireEvent(
        Job job, Supplier<D> eventSupplier, ListenerInvoker<E, D> invoker)
    {
        // TODO check what events we should have, and what their interfaces can expose
        job.getEventDistributor()
            .distribute(eventSupplier, invoker);
    }

    public static int getShort(byte[] memory, int index)
    {
        int b1 = memory[index] & 0xFF; // mask off sign-extended bits
        int b2 = memory[index + 1] & 0xFF;
        return b1 << (8 * 1) | b2;
    }
}
