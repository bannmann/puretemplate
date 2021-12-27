package org.puretemplate;

import lombok.Value;
import lombok.With;

interface Interpreter
{
    @Value
    class Job
    {
        @With
        TemplateWriter templateWriter;
        EventDistributor eventDistributor;
    }

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
    int exec(ST template, TemplateWriter out, EventDistributor eventDistributor);
}
