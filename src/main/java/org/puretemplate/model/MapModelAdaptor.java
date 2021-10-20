package org.puretemplate.model;

import java.util.Map;

import org.puretemplate.exception.NoSuchPropertyException;

public class MapModelAdaptor implements ModelAdaptor<Map<?, ?>>
{
    @Override
    public Object getProperty(Map<?, ?> model, Object property, String propertyName) throws NoSuchPropertyException
    {
        Object value;
        if (property == null)
        {
            value = getDefaultValue(model);
        }
        else if (containsKey(model, property))
        {
            value = model.get(property);
        }
        else if (containsKey(model, propertyName))
        {
            // if can't find the key, try toString version
            value = model.get(propertyName);
        }
        else if (property.equals("keys"))
        {
            value = model.keySet();
        }
        else if (property.equals("values"))
        {
            value = model.values();
        }
        else
        {
            value = getDefaultValue(model); // not found, use default
        }
        if (value == Maps.DICT_KEY)
        {
            value = property;
        }
        return value;
    }

    private static Boolean containsKey(Map<?, ?> map, Object key)
    {
        try
        {
            return map.containsKey(key);
        }
        catch (ClassCastException ex)
        {
            // Map.containsKey is allowed to throw ClassCastException if the key
            // cannot be compared to keys already in the map.
            return false;
        }
    }

    private static Object getDefaultValue(Map<?, ?> map)
    {
        try
        {
            return map.get(Maps.DEFAULT_KEY);
        }
        catch (ClassCastException ex)
        {
            // Map.containsKey is allowed to throw ClassCastException if the key
            // cannot be compared to keys already in the map.
            return false;
        }
    }
}
