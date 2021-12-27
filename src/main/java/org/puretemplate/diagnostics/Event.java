package org.puretemplate.diagnostics;

import java.util.List;

import org.apiguardian.api.API;
import org.puretemplate.misc.Location;

@API(status = API.Status.INTERNAL)
public interface Event
{
    @API(status = API.Status.EXPERIMENTAL)
    interface EvalExpression extends Event, DistributionTarget
    {
    }

    @API(status = API.Status.EXPERIMENTAL)
    interface EvalTemplate extends Event, DistributionTarget
    {
    }

    @API(status = API.Status.EXPERIMENTAL)
    interface Indent extends Event, DistributionTarget
    {
    }

    /**
     * Data for bytecode instructions as they are executed. <br>
     * <br>
     * Note that adding a Trace listener can cause template behavior to change if e.g. your model objects' {@link
     * Object#toString()} methods are not idempotent.
     */
    @API(status = API.Status.EXPERIMENTAL)
    interface Trace extends Event, DistributionTarget
    {
        Statement getStatement();

        List<String> getStack();

        Location getLocation();

        int getStackPointer();

        int getCurrentLineCharacters();
    }

    @API(status = API.Status.INTERNAL)
    interface DistributionTarget
    {
    }
}
