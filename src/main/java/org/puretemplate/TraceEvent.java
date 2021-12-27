package org.puretemplate;

import java.util.List;

import lombok.Builder;
import lombok.Value;

import org.puretemplate.diagnostics.Event;
import org.puretemplate.diagnostics.Statement;
import org.puretemplate.misc.Location;

import com.google.common.collect.ImmutableList;

@Value
class TraceEvent implements Event.Trace, Distributable<Event.Trace>
{
    Statement statement;
    List<String> stack;
    Location location;
    int stackPointer;
    int currentLineCharacters;

    @Builder
    private TraceEvent(Statement statement, List<String> stack, Location location, int stackPointer, int currentLineCharacters)
    {
        this.statement = statement;
        this.stack = ImmutableList.copyOf(stack);
        this.location = location;
        this.stackPointer = stackPointer;
        this.currentLineCharacters = currentLineCharacters;
    }

    @Override
    public String toString()
    {
        return Trace.class.getSimpleName() +
            "{" +
            location.getTemplateName() +
            "#" +
            statement +
            " | " +
            "stack=" +
            stack +
            ", coordinates=" +
            location.getCoordinates()
                .orElse(null) +
            ", stackPointer=" +
            stackPointer +
            ", currentLineCharacters=" +
            currentLineCharacters +
            '}';
    }
}
