package org.assignment.q4;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * SPECIFICATION (Question 4)
 * Features Required:
 * 1. Thread Safety: Must support concurrent reads/writes (High Concurrency).
 * 2. Atomicity: only one thread fetches from DB for a missing key.
 * 3. Bounded Capacity: Prevent OutOfMemory by limiting size.
 * 4. Eviction Policy: LRU (Least Recently Used) to remove old items when full.
 * 5. Fallback Mechanism: Automatic loading from external source via a Supplier/Function.
 * 6. TTL Support: Entries expire after a specified duration.
 */
public class LRUCache<K, V> implements Cache<K, V> {

    // We use a synchronized wrapper because LinkedHashMap is NOT thread-safe by default.
    // NOTE: Unlike ConcurrentHashMap, LinkedHashMap modifies its internal structure
    // (the linked list order) even on 'get' operations (to move the accessed item to the end).
    // Therefore, we must use a coarse-grained lock (synchronizing the whole map).
    private final Map<K, CacheEntry<V>> storage;

    // The fallback function to load data if a key is missing
    private final Function<K, V> externalLoader;

    // The Time-To-Live duration in milliseconds
    private final long ttlMillis;

    public LRUCache(int capacity, long ttlMillis, Function<K, V> externalLoader) {
        this.externalLoader = externalLoader;
        this.ttlMillis = ttlMillis;

        // --- Configuring LinkedHashMap for LRU ---
        // Parameter 1: initialCapacity
        // Parameter 2: loadFactor (0.75f is standard)
        // The Load Factor is a measure that decides "How full allowed is the Map before we resize it?"
        // Parameter 3: accessOrder = true (CRITICAL!)
        //      false = Insertion Order (FIFO behavior)
        //      true  = Access Order (LRU behavior - moves accessed items to the tail)
        LinkedHashMap<K, CacheEntry<V>> internalMap = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                // This method is invoked automatically by Java after every 'put' operation.
                // Logic: If the map size exceeds the capacity, return true to delete the eldest entry.

                // [Optimization]: We can also actively remove expired items here to free space
                if (eldest.getValue().isExpired()) {
                    return true;
                }

                return size() > capacity;
            }
        };

        // Wrap the map to make it Thread-Safe.
        // This ensures that concurrent reads/writes do not corrupt the linked list structure.
        this.storage = Collections.synchronizedMap(internalMap);
    }

    /**
     * Get a value from the cache. If missing, load it using the external loader.
     *
     * @param key the key to fetch
     * @return the cached value or loaded value
     */
    @Override
    public V get(K key) {
        // [TTL Logic]: Lazy Eviction
        // Before fetching, we check if the key exists but is expired.
        // computeIfPresent is atomic and thread-safe. If expired, we return null (which removes it).
        storage.computeIfPresent(key, (k, entry) -> entry.isExpired() ? null : entry);

        // computeIfAbsent provides Atomicity ("Thundering Herd" protection).
        // 1. If key exists (and wasn't removed above): Returns value and moves it to the end of the list (LRU update).
        // 2. If key missing (or expired): Locks the map, runs externalLoader, puts result, and returns it.
        // Other threads requesting the same key will block until the first thread finishes loading.
        CacheEntry<V> entry = storage.computeIfAbsent(key, k -> {
            V loadedValue = externalLoader.apply(k);
            return new CacheEntry<>(loadedValue, ttlMillis);
        });

        return entry.getValue();
    }

    /**
     * Put a key-value pair into the cache
     *
     * @param key   the key
     * @param value the value
     */
    @Override
    public void put(K key, V value) {
        // Wrap the value with the current timestamp + TTL
        storage.put(key, new CacheEntry<>(value, ttlMillis));
    }

    /**
     * Get the current size of the cache
     *
     * @return number of entries in the cache
     */
    @Override
    public int size() {
        return storage.size();
    }

    /**
     * Clear the entire cache
     */
    @Override
    public void clear() {
        storage.clear();
    }
}