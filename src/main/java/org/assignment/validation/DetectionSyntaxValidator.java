package org.assignment.validation;

import org.assignment.model.DetectionDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for checking the syntax of DetectionEntry objects.
 */
@Component
public class DetectionSyntaxValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return DetectionDTO.class.equals(clazz);
    }

    /**
     * Validates the given DetectionEntry object.
     *
     * @param target the object to validate
     * @param errors contextual state about the validation process
     */
    @Override
    public void validate(Object target, Errors errors) {
        DetectionDTO detectionDTO = (DetectionDTO) target;

        // HTTP Method
        if (!StringUtils.hasText(detectionDTO.getMethod())) {
            errors.rejectValue("method", "field.required", "HTTP Method is missing");
        }

        // Path
        if (!StringUtils.hasText(detectionDTO.getPath())) {
            errors.rejectValue("path", "field.required", "Path is missing");
        }

        // Note: Body/Params can be empty, so no check needed there.
    }

}