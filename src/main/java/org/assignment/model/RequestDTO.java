package org.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing a detection entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
    @JsonProperty("method")
    private String method;
    @JsonProperty("path")
    private String path;
    @JsonProperty("query_params")
    private List<KeyValueStringDTO> queryParams;
    @JsonProperty("headers")
    private List<KeyValueStringDTO> headers;
    @JsonProperty("body")
    private List<KeyValueObjectDTO> body;
}