package org.assignment.controller;

import lombok.extern.slf4j.Slf4j;
import org.assignment.exception.AppErrorCode;
import org.assignment.exception.InvalidDetectionControllerException;
import org.assignment.model.*;
import org.assignment.service.DetectionService;
import org.assignment.validation.DetectionSyntaxValidator;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller to handle Anomaly Detection requests.
 * Exposes an endpoint to validate incoming API requests against learned models.
 */
@Slf4j
@RestController
@RequestMapping("/api/detection")
public class DetectionController {

    private final DetectionService detectionService;
    private final DetectionSyntaxValidator validator;

    public DetectionController(DetectionService detectionService, DetectionSyntaxValidator validator) {
        this.detectionService = detectionService;
        this.validator = validator;
    }

    /**
     * Endpoint to validate an incoming API request for anomalies.
     *
     * @param requestDTO The detectionDTO entry containing request details.
     * @return A list of detected anomalies (empty if none).
     */
    @PostMapping("/validate")
    public ResponseEntity<List<AnomalyDTO>> validateDetection(@RequestBody RequestDTO requestDTO) {
        // Validation phase
        DetectionDTO detectionDTO = mapToDetectionDTO(requestDTO);
        BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(detectionDTO, "detectionDTO");
        validator.validate(detectionDTO, bindingResult);

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            // Logs detailed error but returns standard JSON error response via ExceptionHandler
            log.warn("Invalid detectionDTO request: {}", errors);
            throw new InvalidDetectionControllerException(AppErrorCode.INVALID_JSON_FORMAT, errors);
        }

        // Business logic to detect anomalies
        List<AnomalyDTO> anomalies = detectionService.validateDetection(detectionDTO);

        // Return detected anomalies
        return ResponseEntity.ok(anomalies);
    }

    private DetectionDTO mapToDetectionDTO(RequestDTO requestDTO) {
        DetectionDTO internalDetectionDTO = new DetectionDTO();
        internalDetectionDTO.setPath(requestDTO.getPath());
        internalDetectionDTO.setMethod(requestDTO.getMethod());
        internalDetectionDTO.setHeaders(convertListToStringMap(requestDTO.getHeaders()));
        internalDetectionDTO.setQueryParams(convertListToStringMap(requestDTO.getQueryParams()));
        internalDetectionDTO.setBody(convertListToObjectMap(requestDTO.getBody()));
        return internalDetectionDTO;
    }

    private Map<String, String> convertListToStringMap(List<KeyValueStringDTO> list) {
        if (list == null) {
            return Map.of();
        }
        return list.stream()
                .collect(Collectors.toMap(KeyValueStringDTO::getName, KeyValueStringDTO::getValue));
    }

    private Map<String, Object> convertListToObjectMap(List<KeyValueObjectDTO> list) {
        if (list == null) {
            return Map.of();
        }
        return list.stream()
                .collect(Collectors.toMap(KeyValueObjectDTO::getName, KeyValueObjectDTO::getValue));
    }

}