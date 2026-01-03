package org.assignment.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents an anomaly detected in the request processing.
 */
@Data
@Builder
public class AnomalyDTO {
    private String type;
    private String description;
}
