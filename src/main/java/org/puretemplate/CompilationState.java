package org.puretemplate;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.puretemplate.error.ErrorType;
import org.puretemplate.misc.Interval;

/**
 * Temporary data used during construction and functions that fill it / use it. Result is {@link #impl} {@link
 * CompiledST} object.
 */
class CompilationState
{
    /**
     * The compiled code implementation to fill in.
     */
    CompiledST impl = new CompiledST();

    /**
     * Track unique strings; copy into {@link CompiledST#strings} after compilation.
     */
    StringTable stringtable = new StringTable();

    /**
     * Track instruction location within {@code impl.}{@link CompiledST#instrs instrs} array; this is next address to
     * write to. Byte-addressable memory.
     */
    int ip = 0;

    TokenStream tokens;

    ErrorManager errMgr;

    public CompilationState(ErrorManager errMgr, String name, TokenStream tokens)
    {
        this.errMgr = errMgr;
        this.tokens = tokens;
        impl.name = name;
        impl.prefix = Misc.getPrefix(name);
    }

    public int defineString(String s)
    {
        return stringtable.add(s);
    }

    public void refAttr(Token templateToken, CommonTree id)
    {
        String name = id.getText();
        if (impl.formalArguments != null && impl.formalArguments.get(name) != null)
        {
            FormalArgument arg = impl.formalArguments.get(name);
            int index = arg.index;
            emit1(id, Bytecode.Instruction.LOAD_LOCAL, index);
        }
        else
        {
            if (Language.PREDEFINED_ANON_SUBTEMPLATE_ATTRIBUTES.contains(name))
            {
                errMgr.compileTimeError(ErrorType.REF_TO_IMPLICIT_ATTRIBUTE_OUT_OF_SCOPE, templateToken, id.token);
                emit(id, Bytecode.Instruction.NULL);
            }
            else
            {
                emit1(id, Bytecode.Instruction.LOAD_ATTR, name);
            }
        }
    }

    public void setOption(CommonTree id)
    {
        Interpreter.Option O = Compiler.supportedOptions.get(id.getText());
        emit1(id, Bytecode.Instruction.STORE_OPTION, O.ordinal());
    }

    public void func(Token templateToken, CommonTree id)
    {
        Bytecode.Instruction functionInstruction = Compiler.functions.get(id.getText());
        if (functionInstruction == null)
        {
            errMgr.compileTimeError(ErrorType.NO_SUCH_FUNCTION, templateToken, id.token);
            emit(id, Bytecode.Instruction.POP);
        }
        else
        {
            emit(id, functionInstruction);
        }
    }

    public void emit(Bytecode.Instruction instruction)
    {
        emit(null, instruction);
    }

    public void emit(CommonTree opAST, Bytecode.Instruction instruction)
    {
        ensureCapacity(1);
        if (opAST != null)
        {
            int i = opAST.getTokenStartIndex();
            int j = opAST.getTokenStopIndex();
            int p = ((CommonToken) tokens.get(i)).getStartIndex();
            int q = ((CommonToken) tokens.get(j)).getStopIndex();
            if (!(p < 0 || q < 0))
            {
                impl.sourceMap[ip] = new Interval(p, q);
            }
        }
        impl.instrs[ip++] = (byte) instruction.opcode;
    }

    public void emit1(CommonTree opAST, Bytecode.Instruction instruction, int arg)
    {
        emit(opAST, instruction);
        ensureCapacity(Bytecode.OPND_SIZE_IN_BYTES);
        writeShort(impl.instrs, ip, (short) arg);
        ip += Bytecode.OPND_SIZE_IN_BYTES;
    }

    public void emit2(CommonTree opAST, Bytecode.Instruction instruction, int arg, int arg2)
    {
        emit(opAST, instruction);
        ensureCapacity(Bytecode.OPND_SIZE_IN_BYTES * 2);
        writeShort(impl.instrs, ip, (short) arg);
        ip += Bytecode.OPND_SIZE_IN_BYTES;
        writeShort(impl.instrs, ip, (short) arg2);
        ip += Bytecode.OPND_SIZE_IN_BYTES;
    }

    public void emit2(CommonTree opAST, Bytecode.Instruction instruction, String s, int arg2)
    {
        int i = defineString(s);
        emit2(opAST, instruction, i, arg2);
    }

    public void emit1(CommonTree opAST, Bytecode.Instruction instruction, String s)
    {
        int i = defineString(s);
        emit1(opAST, instruction, i);
    }

    public void insert(int addr, Bytecode.Instruction instruction, String s)
    {
        ensureCapacity(1 + Bytecode.OPND_SIZE_IN_BYTES);
        int instrSize = 1 + Bytecode.OPND_SIZE_IN_BYTES;
        System.arraycopy(impl.instrs, addr, impl.instrs, addr + instrSize, ip - addr); // make room for opcode, opnd
        int save = ip;
        ip = addr;
        emit1(null, instruction, s);
        ip = save + instrSize;
        // adjust addresses for BR and BRF
        int a = addr + instrSize;
        while (a < ip)
        {
            byte op = impl.instrs[a];
            Bytecode.Instruction I = Bytecode.INSTRUCTIONS[op];
            if (op == Bytecode.Instruction.BR.opcode || op == Bytecode.Instruction.BRF.opcode)
            {
                int opnd = BytecodeDisassembler.getShort(impl.instrs, a + 1);
                writeShort(impl.instrs, a + 1, (short) (opnd + instrSize));
            }
            a += I.operandTypes.length * Bytecode.OPND_SIZE_IN_BYTES + 1;
        }
    }

    public void write(int addr, short value)
    {
        writeShort(impl.instrs, addr, value);
    }

    protected void ensureCapacity(int n)
    {
        if ((ip + n) >= impl.instrs.length)
        {
            // ensure room for full instruction
            byte[] c = new byte[impl.instrs.length * 2];
            System.arraycopy(impl.instrs, 0, c, 0, impl.instrs.length);
            impl.instrs = c;
            Interval[] sm = new Interval[impl.sourceMap.length * 2];
            System.arraycopy(impl.sourceMap, 0, sm, 0, impl.sourceMap.length);
            impl.sourceMap = sm;
        }
    }

    public void indent(CommonTree indent)
    {
        emit1(indent, Bytecode.Instruction.INDENT, indent.getText());
    }

    /**
     * Write value at index into a byte array highest to lowest byte, left to right.
     */
    public static void writeShort(byte[] memory, int index, short value)
    {
        memory[index + 0] = (byte) ((value >> (8 * 1)) & 0xFF);
        memory[index + 1] = (byte) (value & 0xFF);
    }
}
