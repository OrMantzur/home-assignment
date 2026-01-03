package org.assignment.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.assignment.model.APIModelDTO;
import org.assignment.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache for storing API models using Guava's LoadingCache.
 * <p>
 * This cache automatically loads models from the ModelRepository on cache misses.
 * It uses LRU eviction based on maximum size and TTL (time-to-live) settings.
 * </p>
 */
@Slf4j
@Component
public class ModelCache {

    private final LoadingCache<String, Optional<APIModelDTO>> modelCache;

    public ModelCache(ModelRepository repository,
                      @Value("${app.detection-controller.max-cache-models-entry}") long modelCacheSize,
                      @Value("${app.detection-controller.max-cache-models-ttl-millis}") long modelCacheTTLMillis) {
        // --- GUAVA CACHE SETUP ---
        // "Keep up to X value and remove older entry"
        this.modelCache = CacheBuilder.newBuilder()
                .maximumSize(modelCacheSize)
                .expireAfterAccess(modelCacheTTLMillis, TimeUnit.MILLISECONDS) // LRU Eviction
                // Useful for monitoring hit-rate
                .recordStats()
                .build(new CacheLoader<>() {
                    @Override
                    public Optional<APIModelDTO> load(String key) {
                        // repository.findByKey returns APIModelDTO or null
                        // Optional.ofNullable handles the null safely without exceptions
                        return Optional.ofNullable(repository.findByKey(key));
                    }
                });
    }

    /**
     * Retrieves an API model from the cache by its key.
     *
     * @param key The key representing the combination of HTTP method and path.
     * @return An Optional containing the corresponding APIModelDTO if found, or empty if not found.
     */
    public Optional<APIModelDTO> get(String key) throws ExecutionException {
        return modelCache.get(key);
    }

    /**
     * Evicts an entry from the cache by its key.
     *
     * @param key The key representing the combination of HTTP method and path.
     */
    public void evict(String key) {
        modelCache.invalidate(key);
    }

}
