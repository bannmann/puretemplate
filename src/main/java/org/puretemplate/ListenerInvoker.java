package org.puretemplate;

import java.util.function.BiConsumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.puretemplate.diagnostics.Event;
import org.puretemplate.diagnostics.EventListener;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ListenerInvoker<E extends Event, D extends Distributable<E>>
{
    static final ListenerInvoker<Event.EvalExpression, EvalExpressionEvent> EVAL_EXPRESSION = new ListenerInvoker<>(
        Event.EvalExpression.class,
        EventListener::onEvalExpression);

    static final ListenerInvoker<Event.EvalTemplate, EvalTemplateEvent>
        EVAL_TEMPLATE
        = new ListenerInvoker<>(Event.EvalTemplate.class, EventListener::onEvalTemplate);

    static final ListenerInvoker<Event.Indent, IndentEvent> INDENT = new ListenerInvoker<>(Event.Indent.class,
        EventListener::onIndent);

    static final ListenerInvoker<Event.Trace, TraceEvent> TRACE = new ListenerInvoker<>(Event.Trace.class,
        EventListener::onTrace);

    @Getter
    private final Class<E> eventInterface;

    private final BiConsumer<EventListener, D> invoker;

    public void invoke(D event, EventListener eventListener)
    {
        invoker.accept(eventListener, event);
    }
}
