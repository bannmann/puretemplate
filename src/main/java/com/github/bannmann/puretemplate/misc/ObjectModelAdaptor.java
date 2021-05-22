package com.github.bannmann.puretemplate.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.github.bannmann.puretemplate.Interpreter;
import com.github.bannmann.puretemplate.ModelAdaptor;
import com.github.bannmann.puretemplate.ST;

public class ObjectModelAdaptor<T> implements ModelAdaptor<T>
{
    protected static final Member INVALID_MEMBER;

    static
    {
        Member invalidMember;
        try
        {
            invalidMember = ObjectModelAdaptor.class.getDeclaredField("INVALID_MEMBER");
        }
        catch (NoSuchFieldException ex)
        {
            invalidMember = null;
        }
        catch (SecurityException ex)
        {
            invalidMember = null;
        }

        INVALID_MEMBER = invalidMember;
    }

    protected static final Map<Class<?>, Map<String, Member>>
        membersCache
        = new HashMap<Class<?>, Map<String, Member>>();

    @Override
    public synchronized Object getProperty(Interpreter interp, ST self, T model, Object property, String propertyName)
        throws STNoSuchPropertyException
    {
        if (model == null)
        {
            throw new NullPointerException("o");
        }

        Class<?> c = model.getClass();

        if (property == null)
        {
            return throwNoSuchProperty(c, propertyName, null);
        }

        Member member = findMember(c, propertyName);
        if (member != null)
        {
            try
            {
                if (member instanceof Method)
                {
                    return ((Method) member).invoke(model);
                }
                else if (member instanceof Field)
                {
                    return ((Field) member).get(model);
                }
            }
            catch (Exception e)
            {
                throwNoSuchProperty(c, propertyName, e);
            }
        }

        return throwNoSuchProperty(c, propertyName, null);
    }

    protected static Member findMember(Class<?> clazz, String memberName)
    {
        if (clazz == null)
        {
            throw new NullPointerException("clazz");
        }
        if (memberName == null)
        {
            throw new NullPointerException("memberName");
        }

        synchronized (membersCache)
        {
            Map<String, Member> members = membersCache.get(clazz);
            Member member;
            if (members != null)
            {
                member = members.get(memberName);
                if (member != null)
                {
                    return member != INVALID_MEMBER
                        ? member
                        : null;
                }
            }
            else
            {
                members = new HashMap<String, Member>();
                membersCache.put(clazz, members);
            }

            // try getXXX and isXXX properties, look up using reflection
            String methodSuffix = Character.toUpperCase(memberName.charAt(0)) + memberName.substring(1);

            member = tryGetMethod(clazz, "get" + methodSuffix);
            if (member == null)
            {
                member = tryGetMethod(clazz, "is" + methodSuffix);
                if (member == null)
                {
                    member = tryGetMethod(clazz, "has" + methodSuffix);
                }
            }

            if (member == null)
            {
                // try for a visible field
                member = tryGetField(clazz, memberName);
            }

            members.put(memberName,
                member != null
                    ? member
                    : INVALID_MEMBER);
            return member;
        }
    }

    protected static Method tryGetMethod(Class<?> clazz, String methodName)
    {
        try
        {
            Method method = clazz.getMethod(methodName);
            if (method != null)
            {
                method.setAccessible(true);
            }

            return method;
        }
        catch (Exception ex)
        {
        }

        return null;
    }

    protected static Field tryGetField(Class<?> clazz, String fieldName)
    {
        try
        {
            Field field = clazz.getField(fieldName);
            if (field != null)
            {
                field.setAccessible(true);
            }

            return field;
        }
        catch (Exception ex)
        {
        }

        return null;
    }

    protected Object throwNoSuchProperty(Class<?> clazz, String propertyName, Exception cause)
    {
        throw new STNoSuchPropertyException(cause, null, clazz.getName() + "." + propertyName);
    }
}
