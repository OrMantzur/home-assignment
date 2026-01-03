package org.assignment.controller;

import lombok.extern.slf4j.Slf4j;
import org.assignment.exception.AppErrorCode;
import org.assignment.exception.InvalidModelsControllerException;
import org.assignment.model.APIModelDTO;
import org.assignment.model.APIModelsDTO;
import org.assignment.service.ModelService;
import org.assignment.validation.ModelSyntaxValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller to handle model ingestion requests.
 */
@Slf4j
@RestController
public class ModelController {

    private final ModelService modelService;
    private final ModelSyntaxValidator modelSyntaxValidator;
    private final int maxModelsPerRequest;

    public ModelController(ModelService modelService,
                           ModelSyntaxValidator modelSyntaxValidator,
                           @Value("${app.model-controller.max-models-per-request}") int maxModelsPerRequest
    ) {
        this.modelService = modelService;
        this.modelSyntaxValidator = modelSyntaxValidator;
        this.maxModelsPerRequest = maxModelsPerRequest;
    }


    /**
     * Endpoint to load models into the system.
     *
     * @param apiModelsDTO  The DTO containing the list of models to be ingested.
     * @param bindingResult The binding result to capture validation errors.
     * @return HTTP 200 OK if models are ingested successfully.
     */
    @PostMapping("/api/models")
    public ResponseEntity<Void> loadModels(@RequestBody List<APIModelDTO> apiModelDTOList) {
        // Pre-validation Checks
        if (apiModelDTOList == null || apiModelDTOList.isEmpty()) {
            log.warn("Received empty model list");
            throw new InvalidModelsControllerException(AppErrorCode.EMPTY_MODEL_LIST, "Received list size: 0");
        }

        // Check for maximum allowed models per request
        int modelListSize = apiModelDTOList.size();
        if (modelListSize > maxModelsPerRequest) {
            String errorMsg = String.format("Batch size %d exceeds the maximum allowed limit of %d", modelListSize, maxModelsPerRequest);
            log.warn("Rejected large batch: {}", errorMsg);
            throw new InvalidModelsControllerException(AppErrorCode.MODEL_LIST_TOO_LARGE, errorMsg);
        }

        // Run our Custom Validation
        // This populates 'bindingResult' with any errors found in the list
        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(apiModelDTOList);
        BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(apiModelsDTO, "apiModelsDTO");
        modelSyntaxValidator.validate(apiModelsDTO, bindingResult);

        // 2. Check for Errors
        if (bindingResult.hasErrors()) {
            // Collect all error messages into a single string for debug info
            String errorDetails = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            log.error("Validation failed for models: {}", errorDetails);

            // Throw our custom exception with the details
            throw new InvalidModelsControllerException(AppErrorCode.INVALID_MODEL_SYNTAX, errorDetails);
        }

        // Ingest Models
        log.debug("Ingesting {} models", modelListSize);
        modelService.ingestModels(apiModelsDTO.getApiModelsDTO());
        log.debug("Successfully ingested {} models", modelListSize);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
