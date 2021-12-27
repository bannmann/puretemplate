package org.puretemplate.diagnostics;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;

import lombok.RequiredArgsConstructor;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
@Immutable
public interface Statement
{
    @RequiredArgsConstructor
    enum Format
    {
        PRETTY("%04d:\t%-14s")
            {
                @Override
                protected void beforeOperands(boolean hasAny, StringBuilder buf)
                {
                    if (!hasAny)
                    {
                        buf.append("  ");
                    }
                }
            },
        COMPACT("%04d: %s")
            {
                @Override
                protected void beforeOperands(boolean hasAny, StringBuilder buf)
                {
                    if (hasAny)
                    {
                        buf.append(" ");
                    }
                }
            },
        MINIMAL(null)
            {
                @Override
                protected void beforeOperands(boolean hasAny, StringBuilder buf)
                {
                }

                protected void appendTo(StringBuilder builder, Statement statement)
                {
                    builder.append(statement.getInstruction().formalName)
                        .append(formatStatementOperands(statement));
                }

                private String formatStatementOperands(Statement statement)
                {
                    if (statement.getOperands()
                        .isEmpty())
                    {
                        return "";
                    }
                    return statement.getOperands()
                        .stream()
                        .map(this::formatStatementOperand)
                        .collect(Collectors.joining(" ", " ", ""));
                }

                private String formatStatementOperand(Operand operand)
                {
                    return String.valueOf(operand.getNumericValue());
                }
            };

        private final String formatString;

        protected void appendTo(StringBuilder builder, Statement statement)
        {
            Instruction instruction = statement.getInstruction();
            builder.append(String.format(formatString, statement.getAddress(), instruction.formalName));

            if (instruction.operandTypes.isEmpty())
            {
                beforeOperands(false, builder);
            }
            else
            {
                beforeOperands(true, builder);
                formatOperands(builder, statement);
            }
        }

        protected abstract void beforeOperands(boolean hasAny, StringBuilder buf);

        private void formatOperands(StringBuilder builder, Statement statement)
        {
            String operands = statement.getOperands()
                .stream()
                .map(operand -> operand.getType() == OperandType.STRING
                    ? formatOperand(operand)
                    : String.valueOf(operand.getNumericValue()))
                .collect(Collectors.joining(", "));
            builder.append(operands);
        }

        private String formatOperand(Operand operand)
        {
            String s = "<bad string index>";
            ConstantReference constantReference = operand.getStringConstant();
            if (constantReference.isValid())
            {
                String value = constantReference.getValue();
                if (value == null)
                {
                    s = "null";
                }
                else
                {
                    s = String.format("\"%s\"", replaceEscapes(value));
                }
            }

            return "#" + operand.getNumericValue() + ":" + s;
        }

        private String replaceEscapes(String s)
        {
            s = s.replaceAll("\n", "\\\\n");
            s = s.replaceAll("\r", "\\\\r");
            s = s.replaceAll("\t", "\\\\t");
            return s;
        }
    }

    int getAddress();

    Instruction getInstruction();

    /**
     * @return immutable list of operands
     */
    List<Operand> getOperands();

    int getSize();

    /**
     * Equivalent to {@code toString(Format.COMPACT)}.
     */
    String toString();

    default String toString(Format format)
    {
        StringBuilder result = new StringBuilder();
        appendTo(result, format);
        return result.toString();
    }

    default void appendTo(StringBuilder builder, Format format)
    {
        format.appendTo(builder, this);
    }
}
