package com.github.bannmann.puretemplate.misc;

import com.github.bannmann.puretemplate.compiler.STException;

/** For {@code <a.b>}, object {@code a} does not have a property {@code b}. */
public class STNoSuchPropertyException extends STException
{
    public Object o;
    public String propertyName;
    public STNoSuchPropertyException(Exception e, Object o, String propertyName) {
        super(null, e);
        this.o = o;
        this.propertyName = propertyName;
    }

    @Override
    public String getMessage() {
        if ( o!=null ) return "object "+o.getClass()+" has no "+propertyName+" property";
        else return "no such property: "+propertyName;
    }
}
