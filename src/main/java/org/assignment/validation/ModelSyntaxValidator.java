package org.assignment.validation;

import org.assignment.model.APIModelDTO;
import org.assignment.model.APIModelParamDTO;
import org.assignment.model.APIModelsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Set;

/**
 * Validator for checking the syntax of APIModelsDTO objects.
 * * Note: In a production system, this logic would complement the 3-layer architecture
 * (Guava/Redis/Cassandra) by ensuring that only syntactically valid models are
 * persisted to the Source of Truth (Cassandra).
 */
@Component
public class ModelSyntaxValidator implements Validator {

    public static final String API_MODELS_DTO_FIELD_NAME = "apiModelsDTO";
    private static final Set<String> ALLOWED_METHODS = Set.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"
    );

    private final int maxApiParam;
    private final int maxApiParamType;
    private final int maxStringLength;

    public ModelSyntaxValidator(@Value("${app.model-controller.max-api-param}") int maxApiParam,
                                @Value("${app.model-controller.max-api-param-type}") int maxApiParamType,
                                @Value("${app.model-controller.max-string-length}") int maxStringLength
    ) {
        this.maxApiParam = maxApiParam;
        this.maxApiParamType = maxApiParamType;
        this.maxStringLength = maxStringLength;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return APIModelsDTO.class.equals(clazz);
    }

    /**
     * Validates the given APIModelsDTO object.
     *
     * @param target the object to validate
     * @param errors contextual state about the validation process
     */
    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof APIModelsDTO) {
            APIModelsDTO apiModelsDTO = (APIModelsDTO) target;
            List<APIModelDTO> apiModelList = apiModelsDTO.getApiModelsDTO();
            if (apiModelList != null) {
                for (int i = 0; i < apiModelList.size(); i++) {
                    validateSingleModel(apiModelList.get(i), i, errors);
                }
            }
        }
    }

    /**
     * Validates a single APIModelDTO object.
     *
     * @param model  the APIModelDTO to validate
     * @param index  the index of the model in the list for error path formatting
     * @param errors contextual state about the validation process
     */
    private void validateSingleModel(APIModelDTO model, int index, Errors errors) {
        // Formatted path for the specific model in the list
        String modelPath = String.format("%s[%d]", API_MODELS_DTO_FIELD_NAME, index);

        // 1. Validate Path
        // we can also add security checks for this string
        if (!StringUtils.hasText(model.getPath())) {
            errors.rejectValue(String.format("%s.path", modelPath), "field.required",
                    String.format("Model at index %d is missing 'path'", index));
        } else if (model.getPath().length() > maxStringLength) {
            errors.rejectValue(String.format("%s.path", modelPath), "field.length",
                    String.format("Model at index %d path exceeds maximum length of %d", index, maxStringLength));
        }

        // 2. Validate Method
        if (!StringUtils.hasText(model.getMethod())) {
            errors.rejectValue(String.format("%s.method", modelPath), "field.required",
                    String.format("Model at index %d is missing 'method'", index));
        } else {
            String method = model.getMethod().toUpperCase().trim();
            if (!ALLOWED_METHODS.contains(method)) {
                errors.rejectValue(String.format("%s.method", modelPath), "field.invalid",
                        String.format("Model at index %d has an invalid HTTP method: '%s'. Allowed methods are: %s",
                                index, model.getMethod(), ALLOWED_METHODS));
            }
        }

        // 3. Validate Params
        validateParams(model.getBody(), "body", index, errors);
        validateParams(model.getHeaders(), "headers", index, errors);
        validateParams(model.getQueryParams(), "queryParams", index, errors);
    }

    /**
     * Validates a list of APIModelParamDTO objects.
     *
     * @param params    the list of APIModelParamDTO to validate
     * @param fieldName the name of the field being validated (e.g., "body", "headers", "queryParams")
     * @param index     the index of the model in the list for error path formatting
     * @param errors    contextual state about the validation process
     */
    private void validateParams(List<APIModelParamDTO> params, String fieldName, int index, Errors errors) {
        if (params == null) return;

        String modelFieldPath = String.format("%s[%d].%s", API_MODELS_DTO_FIELD_NAME, index, fieldName);

        if (params.size() > maxApiParam) {
            errors.rejectValue(modelFieldPath, "list.maxsize.exceeded",
                    String.format("Model at index %d has too many parameters in %s (max: %d)", index, fieldName, maxApiParam));
        }

        for (int i = 0; i < params.size(); i++) {
            APIModelParamDTO param = params.get(i);
            String paramPath = String.format("%s[%d]", modelFieldPath, i);

            if (!StringUtils.hasText(param.getName())) {
                errors.rejectValue(String.format("%s.name", paramPath), "field.required",
                        String.format("%s is missing 'name'", paramPath));
            } else if (param.getName().length() > maxStringLength) {
                errors.rejectValue(String.format("%s.name", paramPath), "field.length",
                        String.format("%s name exceeds maximum length of %d", paramPath, maxStringLength));
            }
            validateTypes(param.getTypes(), paramPath, errors);
        }
    }

    /**
     * Validates the types of a single APIModelParamDTO.
     *
     * @param types     the list of types to validate
     * @param paramPath the path of the parameter for error path formatting
     * @param errors    contextual state about the validation process
     */
    private void validateTypes(List<String> types, String paramPath, Errors errors) {
        String typesPath = String.format("%s.types", paramPath);

        if (types == null || types.isEmpty()) {
            errors.rejectValue(typesPath, "field.required", "At least one type is required");
            return;
        }

        // Security Check: Limit number of types per parameter to prevent ReDoS or resource exhaustion
        if (types.size() > maxApiParamType) {
            errors.rejectValue(typesPath, "list.maxsize.exceeded",
                    String.format("Parameter at %s has too many types (max: %d)", paramPath, maxApiParamType));
        }

        // Whitelist Check: Ensure type exists in our supported ValueType Enum
        for (String typeName : types) {
            if (!ValueType.isSupportedType(typeName)) {
                errors.rejectValue(typesPath, "type.unsupported",
                        String.format("Type '%s' at %s is not supported by the system", typeName, typesPath));
            }
        }
    }

}