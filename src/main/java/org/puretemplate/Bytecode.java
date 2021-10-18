package org.puretemplate;

import java.util.Arrays;
import java.util.Locale;

class Bytecode
{
    public static final int OPND_SIZE_IN_BYTES = 2;

    public enum OperandType
    {
        NONE,
        STRING,
        ADDR,
        INT
    }

    public enum Instruction
    {
        LOAD_STR((short) 1, OperandType.STRING),

        LOAD_ATTR((short) 2, OperandType.STRING),

        /**
         * load stuff like it, i, i0
         */
        LOAD_LOCAL((short) 3, OperandType.INT),

        LOAD_PROP((short) 4, OperandType.STRING),

        LOAD_PROP_IND((short) 5),

        STORE_OPTION((short) 6, OperandType.INT),

        STORE_ARG((short) 7, OperandType.STRING),

        /**
         * create new template instance
         */
        NEW((short) 8, OperandType.STRING, OperandType.INT),

        /**
         * create new instance using value on stack
         */
        NEW_IND((short) 9, OperandType.INT),

        /**
         * create new instance using args in Map on stack
         */
        NEW_BOX_ARGS((short) 10, OperandType.STRING),

        /**
         * create new instance using value on stack
         */
        SUPER_NEW((short) 11, OperandType.STRING, OperandType.INT),

        /**
         * create new instance using args in Map on stack
         */
        SUPER_NEW_BOX_ARGS((short) 12, OperandType.STRING),

        WRITE((short) 13),

        WRITE_OPT((short) 14),

        /**
         * <a:b()>, <a:b():c()>, <a:{...}>
         */
        MAP((short) 15),

        /**
         * <a:b(),c()>
         */
        ROT_MAP((short) 16, OperandType.INT),

        /**
         * <names,phones:{n,p | ...}>
         */
        ZIP_MAP((short) 17, OperandType.INT),

        BR((short) 18, OperandType.ADDR),

        BRF((short) 19, OperandType.ADDR),

        /**
         * push options map
         */
        OPTIONS((short) 20),

        /**
         * push args map
         */
        ARGS((short) 21),

        PASSTHRU((short) 22, OperandType.STRING),

        /* PASSTHRU_IND((short)23, OperandType.INT), */

        LIST((short) 24),

        ADD((short) 25),

        TOSTR((short) 26),

        FIRST((short) 27),

        LAST((short) 28),

        REST((short) 29),

        TRUNC((short) 30),

        STRIP((short) 31),

        TRIM((short) 32),

        LENGTH((short) 33),

        STRLEN((short) 34),

        REVERSE((short) 35),

        NOT((short) 36),

        OR((short) 37),

        AND((short) 38),

        INDENT((short) 39, OperandType.STRING),

        DEDENT((short) 40),

        NEWLINE((short) 41),

        /**
         * do nothing
         */
        NOOP((short) 42),

        POP((short) 43),

        /**
         * push null value
         */
        NULL((short) 44),

        /**
         * push true value
         */
        TRUE((short) 45),

        FALSE((short) 46),

        WRITE_STR((short) 47, OperandType.STRING),

        WRITE_LOCAL((short) 48, OperandType.INT);

        /**
         * The instruction bytecode. As byte is signed, we use a short to keep 0..255
         */
        public final short opcode;

        public final OperandType[] operandTypes;

        /**
         * The name used when referencing a function in a template or when disassembling, e.g. "load_str", "new".
         */
        public final String formalName;

        Instruction(short opcode, OperandType... operandTypes)
        {
            this.opcode = opcode;
            this.operandTypes = operandTypes;
            formalName = name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Used for assembly/disassembly; describes instruction set
     */
    public static final Instruction[] INSTRUCTIONS = createLookupArray();

    private static Instruction[] createLookupArray()
    {
        int maxOpCode = Arrays.stream(Instruction.values())
            .mapToInt(instruction -> instruction.opcode)
            .max()
            .orElseThrow(IllegalStateException::new);

        Instruction[] result = new Instruction[maxOpCode + 1];
        for (Instruction instruction : Instruction.values())
        {
            result[instruction.opcode] = instruction;
        }

        return result;
    }
}
