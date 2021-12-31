package org.puretemplate.diagnostics;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface EventListener
{
    default void onEvalExpression(Event.EvalExpression event)
    {
    }

    default void onEvalTemplate(Event.EvalTemplate event)
    {
    }

    default void onIndent(Event.Indent event)
    {
    }

    default void onTrace(Event.Trace event)
    {
    }
}
