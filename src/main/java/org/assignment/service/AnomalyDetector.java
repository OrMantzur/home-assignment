package org.assignment.service;

import org.assignment.model.APIModelDTO;
import org.assignment.model.APIModelParamDTO;
import org.assignment.model.AnomalyDTO;
import org.assignment.model.DetectionDTO;
import org.assignment.validation.TypeValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for detecting anomalies in API requests compared to a learned model.
 */
@Service
public class AnomalyDetector {

    public static final String QUERY_PARAM = "QUERY_PARAM";
    public static final String HEADER = "HEADER";
    public static final String BODY = "BODY";
    public static final String MISSING = "MISSING";
    public static final String DELIMITER = "_";
    public static final String TYPE_MISMATCH = "TYPE_MISMATCH";

    private final TypeValidator typeValidator;

    public AnomalyDetector(TypeValidator typeValidator) {
        this.typeValidator = typeValidator;
    }

    /**
     * Determines if the current request is "abnormal" compared to the learned model.
     * <p>
     * Validates query parameters, headers, and the body of the detection entry against the learned model.
     * Checks for missing required parameters and type mismatches.
     * </p>
     *
     * @param detection    The {@link DetectionDTO} representing the actual request.
     * @param learnedModel The {@link APIModelDTO} representing the expected structure.
     * @return A list of {@link AnomalyDTO} objects describing any discrepancies found.
     * Returns an empty list if no anomalies are detected.
     *
     * <p><strong>Performance Complexity:</strong> O(P * T) where P is the total number of parameters
     * (query + headers + body) in the learned model, and T is the average number of allowed types per parameter.
     * The validation iterates through all defined parameters in the model.</p>
     */
    public List<AnomalyDTO> detectAnomalies(DetectionDTO detection, APIModelDTO learnedModel) {
        List<AnomalyDTO> anomalies = new ArrayList<>();

        // 1. Validate Query Params [cite: 15, 41]
        validateSection(QUERY_PARAM,
                learnedModel.getQueryParams(), detection.getQueryParams(), anomalies);

        // 2. Validate Headers [cite: 21, 46]
        validateSection(HEADER,
                learnedModel.getHeaders(), detection.getHeaders(), anomalies);

        // 3. Validate Body [cite: 26, 51]
        validateSection(BODY,
                learnedModel.getBody(), detection.getBody(), anomalies);

        return anomalies;
    }

    /**
     * Validates a specific section (query params, headers, body) of the detection entry
     * against the learned model parameters.
     *
     * @param sectionName   The name of the section being validated (e.g., "QUERY_PARAM", "HEADER", "BODY").
     * @param learnedParams The list of learned parameters from the model.
     * @param actualValues  The actual values from the detection entry.
     * @param anomalies     The list to which any detected anomalies will be added.
     */
    private void validateSection(String sectionName,
                                 List<APIModelParamDTO> learnedParams,
                                 Map<String, ?> actualValues,
                                 List<AnomalyDTO> anomalies) {

        Map<String, ?> safeActualValues = (actualValues != null) ? actualValues : Collections.emptyMap();
        List<APIModelParamDTO> safeLearnedParams = (learnedParams != null) ? learnedParams : Collections.emptyList();

        for (APIModelParamDTO param : safeLearnedParams) {
            Object actualValue = safeActualValues.get(param.getName());

            // A. Check for missing required parameters [cite: 33, 36]
            if (param.isRequired() && actualValue == null) {
                anomalies.add(AnomalyDTO.builder()
                        // Create specific anomaly types like MISSING_BODY or MISSING_HEADER
                        .type(MISSING + DELIMITER + sectionName)
                        // Format description to match test: "Required field 'X' is missing in Y"
                        .description(String.format("Required field '%s' is missing in %s",
                                param.getName(), sectionName))
                        .build());
                continue;
            }

            // B. Check for type mismatch [cite: 33, 36]
            if (actualValue != null) {
                if (!typeValidator.validate(actualValue, param.getTypes())) {
                    anomalies.add(AnomalyDTO.builder()
                            // Create specific anomaly types like TYPE_MISMATCH_BODY
                            .type(TYPE_MISMATCH + DELIMITER + sectionName)
                            .description(String.format("%s parameter '%s' has value '%s' which does not match any allowed types: %s",
                                    sectionName, param.getName(), actualValue, param.getTypes()))
                            .build());
                }
            }
        }
    }

}