package org.puretemplate;

import java.util.Arrays;

import lombok.experimental.UtilityClass;

import org.puretemplate.diagnostics.Instruction;

@UtilityClass
class Bytecode
{
    public final int OPND_SIZE_IN_BYTES = 2;

    /**
     * Used for assembly/disassembly; describes instruction set
     */
    public final Instruction[] INSTRUCTIONS = createLookupArray();

    private Instruction[] createLookupArray()
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
