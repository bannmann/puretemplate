package org.puretemplate.debug;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public class AddAttributeEvent extends ConstructionEvent
{
    String name;
    /**
     * Reserved for future use.
     */
    Object value;

    public AddAttributeEvent(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "addEvent{" +
            ", name='" +
            name +
            '\'' +
            ", value=" +
            value +
            ", location=" +
            getFileName() +
            ":" +
            getLine() +
            '}';
    }
}
