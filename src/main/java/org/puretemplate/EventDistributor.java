package org.puretemplate;

import java.text.MessageFormat;
import java.util.Set;
import java.util.function.Supplier;

import lombok.NonNull;

import org.puretemplate.diagnostics.Event;
import org.puretemplate.diagnostics.EventListener;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

class EventDistributor
{
    private final SetMultimap<Class<?>, EventListener> listeners = MultimapBuilder.hashKeys()
        .hashSetValues()
        .build();

    public <E extends Event & Event.DistributionTarget> void addEventListener(
        @NonNull EventListener listener, @NonNull Class<E> eventInterface)
    {
        verifyEventInterface(eventInterface);
        listeners.put(eventInterface, listener);
    }

    public <D extends Distributable<E>, E extends Event & Event.DistributionTarget> void distribute(
        @NonNull Supplier<D> eventSupplier, @NonNull ListenerInvoker<E, D> invoker)
    {
        Set<EventListener> targetListeners = listeners.get(invoker.getEventInterface());
        if (targetListeners.isEmpty())
        {
            return;
        }

        D event = eventSupplier.get();
        targetListeners.forEach(eventListener -> invoker.invoke(event, eventListener));
    }

    /**
     * Guard against user code calling {@code addEventListener(..., MyOwnSubclass.class)} or {@code
     * addEventListener(..., MyOwnSubinterface.class)}.
     */
    private <E extends Event> void verifyEventInterface(Class<E> eventInterface)
    {
        if (!eventInterface.isInterface() || !eventInterface.isNestmateOf(Event.class))
        {
            throw new IllegalArgumentException(MessageFormat.format(
                "{0} is not a supported event interface type. Try the interfaces nested inside {1} instead.",
                eventInterface.getName(),
                Event.class.getName()));
        }
    }
}
