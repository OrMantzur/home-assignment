package org.assignment.service;

import org.assignment.model.APIModelDTO;
import org.assignment.model.AnomalyDTO;
import org.assignment.model.DetectionDTO;
import org.assignment.repository.ModelRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing API models and validating detection entries against them.
 */
@Service
public class ModelService {

    public static final String UNKNOWN_ENDPOINT = "UNKNOWN_ENDPOINT";

    private final ModelRepository repository;
    private final AnomalyDetector anomalyDetector;
    private final ModelCache modelCache;

    public ModelService(ModelRepository repository, AnomalyDetector anomalyDetector, ModelCache modelCache) {
        this.repository = repository;
        this.anomalyDetector = anomalyDetector;
        this.modelCache = modelCache;
    }

    /**
     * Ingests a list of API models into the repository.
     * <p>
     * This method processes the input list, deduplicates models based on their method and path,
     * and saves them in a batch operation.
     * </p>
     *
     * @param models The list of {@link APIModelDTO} to be ingested. Can be null or empty.
     *               If null, the operation returns immediately.
     *               Duplicate keys (Method:Path) in the list are handled by keeping the existing entry.
     *
     *               <p><strong>Performance Complexity:</strong> O(N) where N is the number of models in the list,
     *               assuming the map collection and batch save are linear operations.</p>
     */
    public void ingestModels(List<APIModelDTO> models) {
        if (models == null) return;
        Map<String, APIModelDTO> modelMap = models.stream()
                .collect(Collectors.toMap(
                        m -> m.getMethod().toUpperCase() + ":" + m.getPath(), // Key Mapper
                        m -> m,                                               // Value Mapper
                        (existing, replacement) -> existing                   // Merge Function (Handle Duplicates)
                ));
        repository.saveBatch(modelMap);
        // Evict cache entries to ensure consistency
        modelMap.keySet().forEach(modelCache::evict);
    }

    /**
     * Validates a detection entry against the stored API models.
     * <p>
     * Retrieves the corresponding model from the repository using the detection entry's method and path.
     * If the model is not found, a critical anomaly is returned. Otherwise, it delegates to the
     * {@link AnomalyDetector} for detailed validation.
     * </p>
     *
     * @param detectionDTO The {@link DetectionDTO} containing the request details to validate.
     *                       Must contain a valid method and path.
     * @return A list of {@link AnomalyDTO} objects. Returns a list containing a single "UNKNOWN_ENDPOINT"
     * anomaly if the model is not found, or a list of detected anomalies if validation fails.
     *
     * <p><strong>Performance Complexity:</strong> O(1) for repository lookup (assuming hash-based access)
     * plus the complexity of {@link AnomalyDetector#detectAnomalies(DetectionDTO, APIModelDTO)}.</p>
     */
    public List<AnomalyDTO> validateDetection(DetectionDTO detectionDTO) {
        String key = detectionDTO.getMethod().toUpperCase() + ":" + detectionDTO.getPath();
        APIModelDTO model = repository.findByKey(key);

        if (model == null) {
            List<AnomalyDTO> criticalAnomalyDTO = new ArrayList<>();
            criticalAnomalyDTO.add(AnomalyDTO.builder()
//                    .requestId(detectionEntry.getRequestId())
                    .type(UNKNOWN_ENDPOINT)
                    .description("Endpoint not found in learned models")
                    .build());
            return criticalAnomalyDTO;
        }

        return anomalyDetector.detectAnomalies(detectionDTO, model);
    }
}
