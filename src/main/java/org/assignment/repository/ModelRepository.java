package org.assignment.repository;

import org.assignment.model.APIModelDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the Model Repository for assignment purposes.
 * In a real production system, I would use this 3-layer approach:
 * 1. Cassandra(Source of truth): the persistent storage layer. It handles large-scale data and ensures durability.
 * Optimized for write-heavy workloads and read but partition key based access patterns.
 * 2. Redis Cluster(Shared distributed cache): a fast, in-memory cache layer that all application nodes can access.
 * 3. Local Cache(Guava): A small cache inside the application itself.
 */
@Component
public class ModelRepository {

    // Thread safe in memory store
    // Key: "METHOD:PATH"
    // Value: API Model
    private final Map<String, APIModelDTO> modelStore = new ConcurrentHashMap<>();

    /**
     * Save a batch of API models to the repository.
     *
     * @param apiModelDTOS A map where the key is a combination of HTTP method and path,
     *                     and the value is the corresponding APIModelDTO.
     */
    public void saveBatch(Map<String, APIModelDTO> apiModelDTOS) {
        // put is Thread-Safe in ConcurrentHashMap(Bucket Locking)
        modelStore.putAll(apiModelDTOS);
    }

    /**
     * Find an API model by its key.
     *
     * @param key The key representing the combination of HTTP method and path.
     * @return The corresponding APIModelDTO, or null if not found.
     */
    public APIModelDTO findByKey(String key) {
        // get is Lock-Free (High throughput for reads)
        return modelStore.get(key);
    }

}
