package org.assignment.service;

import lombok.extern.slf4j.Slf4j;
import org.assignment.model.APIModelDTO;
import org.assignment.model.AnomalyDTO;
import org.assignment.model.DetectionDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Service for validating detection entries against cached API models.
 */
@Slf4j
@Service
public class DetectionService {

    public static final String UNKNOWN_ENDPOINT = "UNKNOWN_ENDPOINT";

    // The Cache
    private final AnomalyDetector anomalyDetector;
    private final ModelCache modelCache;

    public DetectionService(AnomalyDetector anomalyDetector, ModelCache modelCache) {
        this.anomalyDetector = anomalyDetector;
        this.modelCache = modelCache;
    }

    /**
     * Validates a detection entry using a cached model repository.
     * <p>
     * Attempts to retrieve the API model from an in-memory cache. If the model is not in the cache,
     * it is loaded from the repository. If the model is not found (either in cache or repository),
     * an "UNKNOWN_ENDPOINT" anomaly is returned.
     * </p>
     *
     * @param detection The {@link DetectionDTO} to validate.
     * @return A list of {@link AnomalyDTO} objects. Returns "UNKNOWN_ENDPOINT" if the model is missing,
     * otherwise returns anomalies detected by {@link AnomalyDetector}.
     * Returns an empty list in case of a cache execution error (fail-safe).
     *
     * <p><strong>Performance Complexity:</strong> O(1) for cache lookup (amortized).
     * If a cache miss occurs, it incurs the cost of a repository lookup.
     * Subsequent validation depends on {@link AnomalyDetector#detectAnomalies}.</p>
     */
    public List<AnomalyDTO> validateDetection(DetectionDTO detection) {
        String key = detection.getMethod().toUpperCase() + ":" + detection.getPath();

        try {
            // Use Optional to wrap the result so the Cache stores the Optional object.
            // If the repository returns null, we return Optional.empty().
            // If you truly want to NEVER store the null/empty result, see the manual check below.

            // Atomic lookup: Check Cache -> If miss -> Repo -> Store in Cache
            Optional<APIModelDTO> modelOpt = modelCache.get(key);

            if (modelOpt.isEmpty()) {
                // Requirement: identify abnormal requests like unknown endpoints [cite: 6, 61]
                return List.of(AnomalyDTO.builder()
                        .type(UNKNOWN_ENDPOINT)
                        .description("Endpoint not found in learned models")
                        .build());
            }

            // 2. DETECT
            return anomalyDetector.detectAnomalies(detection, modelOpt.get());

        } catch (ExecutionException e) {
            // If repository returned null (ResourceNotFoundException), we handle it here
            if (e.getCause() instanceof ResourceNotFoundException) {
                // UNKNOWN ENDPOINT Case
                return List.of(AnomalyDTO.builder()
                        .type(UNKNOWN_ENDPOINT)
                        .description("Endpoint not found in learned models")
                        .build());
            }
            log.error("Cache read error", e);
            // Fail safe
            return Collections.emptyList();
        }
    }

    // Internal marker exception for CacheLoader
    private static class ResourceNotFoundException extends RuntimeException {
    }

}