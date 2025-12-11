package org.assignment.q4;

/**
 * Cache interface
 *
 * @param <K>
 * @param <V>
 */
public interface Cache<K, V> {

    V get(K key);

    void put(K key, V value);

    void clear();

    int size();

}
