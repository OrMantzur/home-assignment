package org.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing a parameter in an API model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIModelParamDTO {
    private String name;
    private List<String> types;
    private boolean required;
}
