package org.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object representing a collection of API models.
 */
@Data
public class APIModelsDTO {
    @JsonProperty("models")
    private List<APIModelDTO> apiModelsDTO;
}
