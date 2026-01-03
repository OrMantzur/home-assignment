package org.assignment.validation;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The core validation engine.
 * Optimized for high-traffic real-time processing.
 */
@Component
public class TypeValidator {

    /**
     * Determines if a value is valid against a list of allowed types.
     * * @param value The raw string value from the request.
     *
     * @param allowedTypeNames The list of valid types from the API model.
     * @return true if the value matches at least one type, false otherwise.
     */
    public boolean validate(Object value, List<String> allowedTypeNames) {
        // If no types are defined, we consider it valid (no constraints).
        if (allowedTypeNames == null || allowedTypeNames.isEmpty()) {
            return true;
        }

        // If a value is missing but the type list is not empty,
        // this is a type mismatch/null error.
        if (value == null) {
            return false;
        }

        String strVal = String.valueOf(value);

        // Principal Engineer approach: Iterate and exit as soon as one type matches (OR logic).
        for (String typeName : allowedTypeNames) {
            ValueType strategy = ValueType.get(typeName);

            // If the strategy exists and the value matches the specific format.
            if (strategy != null && strategy.isValid(strVal)) {
                return true;
            }
        }

        return false;
    }

}