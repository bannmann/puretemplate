package org.puretemplate.model;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

import org.apiguardian.api.API;
import org.puretemplate.exception.NoSuchPropertyException;

@API(status = API.Status.STABLE)
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
        catch (NoSuchFieldException | SecurityException ex)
        {
            invalidMember = null;
        }

        INVALID_MEMBER = invalidMember;
    }

    protected static final Map<Class<?>, Map<String, Member>> membersCache = new HashMap<>();

    @Override
    public synchronized Object getProperty(@NonNull T model, Object property, String propertyName)
        throws NoSuchPropertyException
    {
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

    protected static Member findMember(@NonNull Class<?> clazz, @NonNull String memberName)
    {
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
                members = new HashMap<>();
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
        throw new NoSuchPropertyException(cause, null, clazz.getName() + "." + propertyName);
    }
}
