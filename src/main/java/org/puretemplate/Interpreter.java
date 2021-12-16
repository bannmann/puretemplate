package org.puretemplate;

import java.util.List;

interface Interpreter
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
