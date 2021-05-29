package com.github.bannmann.puretemplate;

import java.io.Closeable;
import java.io.IOException;

import com.github.bannmann.puretemplate.compiler.Bytecode;

/**
 * Generic StringTemplate output writer filter.
 * <p>
 * Literals and the elements of expressions are emitted via {@link #write(String)}. Separators are emitted via {@link
 * #writeSeparator(String)} because they must be handled specially when wrapping lines (we don't want to wrap in between
 * an element and it's separator).</p>
 */
public interface STWriter extends Closeable
{
    int NO_WRAP = -1;

    void pushIndentation(String indent);

    String popIndentation();

    void pushAnchorPoint();

    void popAnchorPoint();

    void setLineWidth(int lineWidth);

    /**
     * Write the string and return how many actual characters were written. With auto-indentation and wrapping, more
     * chars than {@code str.length()} can be emitted.  No wrapping is done.
     */
    int write(String str) throws IOException;

    /**
     * Same as write, but wrap lines using the indicated string as the wrap character (such as {@code "\n"}).
     */
    int write(String str, String wrap) throws IOException;

    /**
     * Because we evaluate ST instance by invoking {@link Interpreter#exec(STWriter, InstanceScope)} again, we can't
     * pass options in. So the {@link Bytecode#INSTR_WRITE} instruction of an applied template (such as when we wrap in
     * between template applications like {@code <data:{v|[<v>]}; wrap>}) we need to write the {@code wrap} string
     * before calling {@link Interpreter#exec}. We expose just like for the separator. See {@link
     * Interpreter#writeObject} where it checks for ST instance. If POJO, {@link Interpreter#writePOJO} passes {@code
     * wrap} to {@link STWriter#write(String str, String wrap)}. Can't pass to {@link Interpreter#exec}.
     */
    int writeWrap(String wrap) throws IOException;

    /**
     * Write a separator.  Same as {@link #write(String)} except that a {@code "\n"} cannot be inserted before emitting
     * a separator.
     */
    int writeSeparator(String str) throws IOException;

    /**
     * Return the absolute char index into the output of the char we're about to write.  Returns 0 if no char written
     * yet.
     */
    int index();
}
