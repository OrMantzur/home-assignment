package org.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object representing a detection entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionDTO {
    @JsonProperty("method")
    private String method;
    @JsonProperty("path")
    private String path;
    @JsonProperty("query_params")
    private Map<String, String> queryParams;
    @JsonProperty("headers")
    private Map<String, String> headers;
    @JsonProperty("body")
    private Map<String, Object> body;
}