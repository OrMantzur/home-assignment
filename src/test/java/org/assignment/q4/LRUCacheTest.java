package org.assignment.q4;

import org.assignment.q4.LRUCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Test Suite for LRUCache
 * <p>
 * This suite covers:
 * 1. Basic Operations (CRUD)
 * 2. Cache Hit vs. Cache Miss behavior
 * 3. LRU Eviction Policy
 * 4. Access Order updates
 * 5. Concurrency and Thundering Herd Protection
 */
public class LRUCacheTest {

    // A long TTL (1 Hour) for standard LRU tests to ensure they don't expire mid-test
    private static final long NO_EXPIRATION_TTL = 3600 * 1000L;

    // --- 1. Basic Operations Test (CRUD) ---
    @Test
    void testBasicCacheOperations() {
        // Updated Constructor: Capacity=5, TTL=1 Hour
        LRUCache<Integer, String> cache = new LRUCache<>(5, NO_EXPIRATION_TTL, key -> "Value-" + key);

        // Test Auto-Loading (Cache Miss)
        assertEquals("Value-1", cache.get(1));
        assertEquals(1, cache.size());

        // Test Explicit Put
        cache.put(2, "Manual-Value");
        assertEquals("Manual-Value", cache.get(2));
        assertEquals(2, cache.size());

        // Test Clear
        cache.clear();
        assertEquals(0, cache.size());
    }

    // --- 2. Test Cache Hit vs. Cache Miss ---
    @Test
    void testCacheHitAvoidsLoader() {
        AtomicInteger loadCounter = new AtomicInteger(0);

        Function<String, String> trackingLoader = key -> {
            loadCounter.incrementAndGet();
            return "Computed-" + key;
        };

        // Updated Constructor
        LRUCache<String, String> cache = new LRUCache<>(3, NO_EXPIRATION_TTL, trackingLoader);

        // 1. First fetch (Miss) -> Should call loader
        String val1 = cache.get("A");
        assertEquals("Computed-A", val1);
        assertEquals(1, loadCounter.get(), "Loader should run once for new key");

        // 2. Second fetch (Hit) -> Should NOT call loader
        String val2 = cache.get("A");
        assertEquals("Computed-A", val2);
        assertEquals(1, loadCounter.get(), "Loader should NOT run for existing key");
    }

    // --- 3. Test LRU Eviction (Capacity Limit) ---
    @Test
    void testLruEviction_WhenCacheIsFull() {
        // Updated Constructor
        LRUCache<Integer, String> cache = new LRUCache<>(2, NO_EXPIRATION_TTL, k -> "V" + k);

        // Fill cache [1, 2]
        cache.get(1);
        cache.get(2);

        // Add 3rd item. This should force eviction of key 1.
        cache.get(3);

        assertEquals(2, cache.size(), "Size should remain at capacity limit");

        // Verify via behavior:
        // '2' was accessed recently, '3' is new. '1' is oldest.
        // If we add 4, '2' should be evicted because we haven't touched it in a while.
        cache.get(4);
        // Cache state: [3, 4]. '2' was evicted.
    }

    // --- 4. Test Access Order (LRU Logic) ---
    @Test
    void testAccessOrderUpdatesLru() {
        // Updated Constructor
        LRUCache<Integer, String> cache = new LRUCache<>(2, NO_EXPIRATION_TTL, k -> "V" + k);

        // 1. Add 1 and 2. Order: [1 (Old), 2 (New)]
        cache.get(1);
        cache.get(2);

        // 2. Access key 1 again. Order changes to: [2 (Old), 1 (New)]
        cache.get(1);

        // 3. Add key 3. This should evict the oldest (2), NOT 1.
        cache.get(3);

        // Verify with side-effects loader
        AtomicInteger reloadCount = new AtomicInteger(0);
        LRUCache<Integer, String> spyCache = new LRUCache<>(2, NO_EXPIRATION_TTL, k -> {
            reloadCount.incrementAndGet();
            return "V" + k;
        });

        spyCache.get(10); // Load 10
        spyCache.get(20); // Load 20
        spyCache.get(10); // Access 10 -> [20 (Old), 10 (New)]
        spyCache.get(30); // Load 30 -> Evicts 20.

        int currentLoads = reloadCount.get(); // Should be 3 (10, 20, 30)

        spyCache.get(10); // Should be in cache (Hit)
        assertEquals(currentLoads, reloadCount.get(), "Key 10 should be in cache (saved by access)");

        spyCache.get(20); // Should be evicted (Miss -> Reload)
        assertEquals(currentLoads + 1, reloadCount.get(), "Key 20 should have been evicted and reloaded");
    }

    // --- 5. Test Concurrency (Thundering Herd Protection) ---
    @Test
    void testThunderingHerdProtection() throws InterruptedException, ExecutionException {
        int threadsCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        AtomicInteger databaseCalls = new AtomicInteger(0);

        Function<String, String> slowLoader = k -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            databaseCalls.incrementAndGet();
            return "Value";
        };

        // Updated Constructor
        LRUCache<String, String> cache = new LRUCache<>(100, NO_EXPIRATION_TTL, slowLoader);

        Callable<String> task = () -> cache.get("KEY_X");

        java.util.List<Callable<String>> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < threadsCount; i++) {
            tasks.add(task);
        }

        java.util.List<Future<String>> results = executor.invokeAll(tasks);

        for (Future<String> res : results) {
            assertEquals("Value", res.get());
        }

        assertEquals(1, databaseCalls.get(), "Thundering Herd Protection Failed! DB was hit multiple times.");

        executor.shutdown();
    }

    // --- 6. Test TTL Expiration ---
    @Test
    void testTtlExpiration() throws InterruptedException {
        // Set a short TTL (200ms)
        long shortTtl = 200L;
        AtomicInteger loadCounter = new AtomicInteger(0);

        LRUCache<Integer, String> cache = new LRUCache<>(5, shortTtl, k -> {
            loadCounter.incrementAndGet();
            return "Value-" + k + "-" + System.currentTimeMillis();
        });

        // 1. Initial Load
        String val1 = cache.get(1);
        assertEquals(1, loadCounter.get());

        // 2. Immediate access (Cache Hit)
        String val2 = cache.get(1);
        assertEquals(val1, val2);
        assertEquals(1, loadCounter.get(), "Should hit cache immediately");

        // 3. Wait for TTL to expire (> 200ms)
        Thread.sleep(300);

        // 4. Access again (Cache Miss -> Expired -> Reload)
        String val3 = cache.get(1);

        assertEquals(2, loadCounter.get(), "Should reload from source after TTL");
        assertNotEquals(val1, val3, "Value should be refreshed (timestamp changed in loader)");
    }

    /**
     * Test that putting an item resets its TTL
     */
    @Test
    void testTtlResetOnPut() throws InterruptedException {
        // Set TTL 300ms
        long ttl = 300L;
        LRUCache<Integer, String> cache = new LRUCache<>(5, ttl, k -> "Loaded");

        // 1. Put initial value
        cache.put(1, "Initial");

        // 2. Wait 200ms (Total: 200ms) - Still alive
        Thread.sleep(200);

        // 3. Update value manually (This should restart the TTL timer)
        cache.put(1, "Updated");

        // 4. Wait another 200ms (Total time since start: 400ms, but only 200ms since last update)
        Thread.sleep(200);

        // 5. Get - Should NOT be expired yet because the 'put' reset the clock
        String result = cache.get(1);
        assertEquals("Updated", result, "Item should remain in cache because 'put' reset the TTL");

        // 6. Wait another 200ms (Total since update: 400ms > 300ms)
        Thread.sleep(200);

        // 7. Get - Should be expired now, triggering the loader
        String reloaded = cache.get(1);
        assertEquals("Loaded", reloaded, "Item should expire after 300ms from the last update");
    }

}