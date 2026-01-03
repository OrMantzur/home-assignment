package org.assignment.controller;

import lombok.extern.slf4j.Slf4j;
import org.assignment.exception.AppErrorCode;
import org.assignment.exception.InvalidDetectionControllerException;
import org.assignment.model.AnomalyDTO;
import org.assignment.model.DetectionDTO;
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
     * @param detectionDTO     The detectionDTO entry containing request details.
     * @param bindingResult The binding result to capture validation errors.
     * @return A list of detected anomalies (empty if none).
     */
    @PostMapping("/validate")
    public ResponseEntity<List<AnomalyDTO>> validateDetection(@RequestBody DetectionDTO detectionDTO,
                                                              BindingResult bindingResult) {
        // Validation phase
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

}