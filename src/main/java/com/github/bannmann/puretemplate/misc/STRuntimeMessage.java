package com.github.bannmann.puretemplate.misc;

import com.github.bannmann.puretemplate.InstanceScope;
import com.github.bannmann.puretemplate.InstanceScopes;
import com.github.bannmann.puretemplate.Interpreter;

/**
 * Used to track errors that occur in the ST interpreter.
 */
public class STRuntimeMessage extends STMessage
{
    /**
     * Which interpreter was executing?  If {@code null}, can be IO error or bad URL etc...
     */
    final Interpreter interp;

    /**
     * Where error occurred in bytecode memory.
     */
    public final int ip;

    public final InstanceScope scope;

    public STRuntimeMessage(Interpreter interp, ErrorType error, int ip)
    {
        this(interp, error, ip, null);
    }

    public STRuntimeMessage(Interpreter interp, ErrorType error, int ip, InstanceScope scope)
    {
        this(interp, error, ip, scope, null);
    }

    public STRuntimeMessage(Interpreter interp, ErrorType error, int ip, InstanceScope scope, Object arg)
    {
        this(interp, error, ip, scope, null, arg, null);
    }

    public STRuntimeMessage(Interpreter interp, ErrorType error, int ip, InstanceScope scope, Throwable e, Object arg)
    {
        this(interp, error, ip, scope, e, arg, null);
    }

    public STRuntimeMessage(
        Interpreter interp, ErrorType error, int ip, InstanceScope scope, Throwable e, Object arg, Object arg2)
    {
        this(interp, error, ip, scope, e, arg, arg2, null);
    }

    public STRuntimeMessage(
        Interpreter interp,
        ErrorType error,
        int ip,
        InstanceScope scope,
        Throwable e,
        Object arg,
        Object arg2,
        Object arg3)
    {
        super(error,
            scope != null
                ? scope.st
                : null,
            e,
            arg,
            arg2,
            arg3);
        this.interp = interp;
        this.ip = ip;
        this.scope = scope;
    }

    /**
     * Given an IP (code location), get it's range in source template then return it's template line:col.
     */
    public String getSourceLocation()
    {
        if (ip < 0 || self == null || self.impl == null)
        {
            return null;
        }
        Interval I = self.impl.sourceMap[ip];
        if (I == null)
        {
            return null;
        }
        // get left edge and get line/col
        int i = I.a;
        Coordinate loc = Misc.getLineCharPosition(self.impl.template, i);
        return loc.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        String loc = null;
        if (self != null)
        {
            loc = getSourceLocation();
            buf.append("context [");
            if (interp != null)
            {
                buf.append(InstanceScopes.getEnclosingInstanceStackString(scope));
            }
            buf.append("]");
        }
        if (loc != null)
        {
            buf.append(" " + loc);
        }
        buf.append(" " + super.toString());
        return buf.toString();
    }
}
