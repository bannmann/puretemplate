package com.github.bannmann.puretemplate.misc;

import java.util.*;

/** A hash table that maps a key to a list of elements not just a single. */
public class MultiMap<K, V> extends LinkedHashMap<K, List<V>> {
    public void map(K key, V value) {
        List<V> elementsForKey = get(key);
        if ( elementsForKey==null ) {
            elementsForKey = new ArrayList<V>();
            super.put(key, elementsForKey);
        }
        elementsForKey.add(value);
    }
}
