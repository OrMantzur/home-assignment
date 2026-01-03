package org.assignment.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing an API model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIModelDTO {
    @JsonProperty("path")
    private String path;
    @JsonProperty("method")
    private String method;
    @JsonProperty("query_params")
    private List<APIModelParamDTO> queryParams;
    @JsonProperty("headers")
    private List<APIModelParamDTO> headers;
    @JsonProperty("body")
    private List<APIModelParamDTO> body;
}