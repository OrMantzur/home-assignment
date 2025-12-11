package org.assignment.q4;

import lombok.Getter;

/**
 * A cache entry that holds the value and its expiry time.
 *
 * @param <V> The type of the cached value.
 */
@Getter
public class CacheEntry<V> {

    private final V value;
    private final long expiryTime;

    CacheEntry(V value, long ttlMillis) {
        this.value = value;
        this.expiryTime = System.currentTimeMillis() + ttlMillis;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

}
