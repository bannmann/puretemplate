package com.github.bannmann.puretemplate.compiler;

import com.github.bannmann.puretemplate.misc.Interval;
import com.github.bannmann.puretemplate.misc.Misc;

import java.util.ArrayList;
import java.util.List;

public class BytecodeDisassembler {
    CompiledST code;

    public BytecodeDisassembler(CompiledST code) { this.code = code; }

    public String instrs() {
        StringBuilder buf = new StringBuilder();
        int ip=0;
        while (ip<code.codeSize) {
            if ( ip>0 ) buf.append(", ");
            int opcode = code.instrs[ip];
            Bytecode.Instruction I = Bytecode.instructions[opcode];
            buf.append(I.name);
            ip++;
            for (int opnd=0; opnd<I.nopnds; opnd++) {
                buf.append(' ');
                buf.append(getShort(code.instrs, ip));
                ip += Bytecode.OPND_SIZE_IN_BYTES;
            }
        }
        return buf.toString();
    }

    public String disassemble() {
        StringBuilder buf = new StringBuilder();
        int i=0;
        while (i<code.codeSize) {
            i = disassembleInstruction(buf, i);
            buf.append('\n');
        }
        return buf.toString();
    }

    public int disassembleInstruction(StringBuilder buf, int ip) {
        int opcode = code.instrs[ip];
        if ( ip>=code.codeSize ) {
            throw new IllegalArgumentException("ip out of range: "+ip);
        }
        Bytecode.Instruction I =
            Bytecode.instructions[opcode];
        if ( I==null ) {
            throw new IllegalArgumentException("no such instruction "+opcode+
                " at address "+ip);
        }
        String instrName = I.name;
        buf.append( String.format("%04d:\t%-14s", ip, instrName) );
        ip++;
        if ( I.nopnds ==0 ) {
            buf.append("  ");
            return ip;
        }
        List<String> operands = new ArrayList<String>();
        for (int i=0; i<I.nopnds; i++) {
            int opnd = getShort(code.instrs, ip);
            ip += Bytecode.OPND_SIZE_IN_BYTES;
            switch ( I.type[i] ) {
                case STRING :
                    operands.add(showConstPoolOperand(opnd));
                    break;
                case ADDR :
                case INT :
                    operands.add(String.valueOf(opnd));
                    break;
                default:
                    operands.add(String.valueOf(opnd));
                    break;
            }
        }
        for (int i = 0; i < operands.size(); i++) {
            String s = operands.get(i);
            if ( i>0 ) buf.append(", ");
            buf.append( s );
        }
        return ip;
    }

    private String showConstPoolOperand(int poolIndex) {
        StringBuilder buf = new StringBuilder();
        buf.append("#");
        buf.append(poolIndex);
        String s = "<bad string index>";
        if ( poolIndex<code.strings.length ) {
            if ( code.strings[poolIndex]==null ) s = "null";
            else {
                s = code.strings[poolIndex];
                if (code.strings[poolIndex] != null) {
                    s = Misc.replaceEscapes(s);
                    s='"'+s+'"';
                }
            }
        }
        buf.append(":");
        buf.append(s);
        return buf.toString();
    }

    public static int getShort(byte[] memory, int index) {
        int b1 = memory[index]&0xFF; // mask off sign-extended bits
        int b2 = memory[index+1]&0xFF;
        int word = b1<<(8*1) | b2;
        return word;
    }

    public String strings() {
        StringBuilder buf = new StringBuilder();
        int addr = 0;
        if ( code.strings!=null ) {
            for (Object o : code.strings) {
                if ( o instanceof String ) {
                    String s = (String)o;
                    s = Misc.replaceEscapes(s);
                    buf.append( String.format("%04d: \"%s\"\n", addr, s) );
                }
                else {
                    buf.append( String.format("%04d: %s\n", addr, o) );
                }
                addr++;
            }
        }
        return buf.toString();
    }

    public String sourceMap() {
        StringBuilder buf = new StringBuilder();
        int addr = 0;
        for (Interval I : code.sourceMap) {
            if ( I!=null ) {
                String chunk = code.template.substring(I.a,I.b+1);
                buf.append( String.format("%04d: %s\t\"%s\"\n", addr, I, chunk) );
            }
            addr++;
        }
        return buf.toString();
    }
}
