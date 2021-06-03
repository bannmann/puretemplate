package com.github.bannmann.puretemplate;

import java.util.List;

import com.github.bannmann.puretemplate.debug.InterpEvent;

public interface Interpreter
{
    enum Option
    {
        ANCHOR,
        FORMAT,
        NULL,
        SEPARATOR,
        WRAP
    }

    /**
     * Execute the given template and return how many characters it wrote to {@code out}.
     *
     * @param template the template to execute
     * @param out the target writer
     *
     * @return the number of characters written to {@code out}
     */
    int exec(ST template, STWriter out);

    List<InterpEvent> getEvents();

    List<String> getExecutionTrace();
}
