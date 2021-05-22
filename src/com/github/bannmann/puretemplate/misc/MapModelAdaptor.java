package com.github.bannmann.puretemplate.misc;

import com.github.bannmann.puretemplate.Interpreter;
import com.github.bannmann.puretemplate.ModelAdaptor;
import com.github.bannmann.puretemplate.ST;
import com.github.bannmann.puretemplate.STGroup;

import java.util.Map;

public class MapModelAdaptor implements ModelAdaptor<Map<?, ?>> {
    @Override
    public Object getProperty(Interpreter interp, ST self, Map<?, ?> model, Object property, String propertyName)
        throws STNoSuchPropertyException
    {
        Object value;
        if ( property==null ) value = getDefaultValue(model);
        else if ( containsKey(model, property) ) value = model.get(property);
        else if ( containsKey(model, propertyName) ) { // if can't find the key, try toString version
            value = model.get(propertyName);
        }
        else if ( property.equals("keys") ) value = model.keySet();
        else if ( property.equals("values") ) value = model.values();
        else value = getDefaultValue(model); // not found, use default
        if ( value == STGroup.DICT_KEY ) {
            value = property;
        }
        return value;
    }

    private static Boolean containsKey(Map<?, ?> map, Object key) {
        try {
            return map.containsKey(key);
        }
        catch (ClassCastException ex) {
            // Map.containsKey is allowed to throw ClassCastException if the key
            // cannot be compared to keys already in the map.
            return false;
        }
    }

    private static Object getDefaultValue(Map<?, ?> map) {
        try {
            return map.get(STGroup.DEFAULT_KEY);
        }
        catch (ClassCastException ex) {
            // Map.containsKey is allowed to throw ClassCastException if the key
            // cannot be compared to keys already in the map.
            return false;
        }
    }
}
