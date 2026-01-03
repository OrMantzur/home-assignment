package org.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assignment.model.APIModelDTO;
import org.assignment.model.KeyValueObjectDTO;
import org.assignment.model.RequestDTO;
import org.assignment.model.KeyValueStringDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the API Anomaly Detector focusing on DetectionEntry validation.
 */
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
@AutoConfigureMockMvc
class APIAnomalyDTODetectorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupLearningPhase() throws Exception {
        ClassPathResource resource = new ClassPathResource("api-models/model_list.json");
        String modelsJson = Files.readString(resource.getFile().toPath());

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modelsJson))
                .andExpect(status().isOk());
    }

    @Test
    void testValidDetectionFlows() throws Exception {
        // Scenario 1: Valid Login (Body Check)
        RequestDTO validLogin = new RequestDTO();
        validLogin.setMethod("POST");
        validLogin.setPath("/api/login");
        validLogin.setBody(List.of(
                new KeyValueObjectDTO("username", "john_doe"), // Quoted String
                new KeyValueObjectDTO("password", "secret123"), // Quoted String
                new KeyValueObjectDTO("is_admin", "true")             // Raw Boolean
        ));

        sendAndExpectValid(validLogin);

        // Scenario 2: Valid Payment (Header & Query Param Check)
        RequestDTO validPayment = new RequestDTO();
        validPayment.setMethod("POST");
        validPayment.setPath("/api/payment");
        validPayment.setQueryParams(List.of(new KeyValueStringDTO("amount", "500"))); // Raw Int
        validPayment.setHeaders(List.of(
                new KeyValueStringDTO("X-Transaction-ID", "550e8400-e29b-41d4-a716-446655440000"), // Quoted UUID
                new KeyValueStringDTO("X-Auth-Token", "Bearer myToken123")                         // Quoted Auth-Token
        ));

        sendAndExpectValid(validPayment);
    }

    @Test
    void testAnomalyDetectionFlows() throws Exception {
        // Scenario 1: Unknown Endpoint (Shadow API)
        RequestDTO unknownPath = new RequestDTO();
        unknownPath.setMethod("DELETE");
        unknownPath.setPath("/api/database/drop");

        mockMvc.perform(post("/api/detection/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownPath)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("UNKNOWN_ENDPOINT"));

        // Scenario 2: Missing Required Field (Body)
        RequestDTO missingPass = new RequestDTO();
        missingPass.setMethod("POST");
        missingPass.setPath("/api/login");
        missingPass.setBody(List.of(new KeyValueObjectDTO("username", "hacker"))); // Missing 'password'

        mockMvc.perform(post("/api/detection/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingPass)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("MISSING_BODY"));

        // Scenario 3: Type Mismatch (Header UUID)
        RequestDTO badUuid = new RequestDTO();
        badUuid.setMethod("POST");
        badUuid.setPath("/api/payment");
        badUuid.setQueryParams(List.of(new KeyValueStringDTO("amount", "100")));
        badUuid.setHeaders(List.of(
                new KeyValueStringDTO("X-Auth-Token", "ok"),
                new KeyValueStringDTO("X-Transaction-ID", "not-a-uuid-just-string") // Quoted but invalid content
        ));

        mockMvc.perform(post("/api/detection/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badUuid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TYPE_MISMATCH_HEADER"));
    }

    @Test
    void testPolymorphismValidation() throws Exception {
        // Case A: flexible_id as INT (Valid)
        RequestDTO detectionAsInt = new RequestDTO();
        detectionAsInt.setMethod("GET");
        detectionAsInt.setPath("/api/search");
        detectionAsInt.setQueryParams(List.of(
                new KeyValueStringDTO("q", "shoes"),
                new KeyValueStringDTO("flexible_id", "12345") // Raw Int
        ));

        sendAndExpectValid(detectionAsInt);

        // Case B: flexible_id as STRING (Valid)
        RequestDTO detectionAsString = new RequestDTO();
        detectionAsString.setMethod("GET");
        detectionAsString.setPath("/api/search");
        detectionAsString.setQueryParams(List.of(
                new KeyValueStringDTO("q", "shoes"),
                new KeyValueStringDTO("flexible_id", "SKU-999") // Quoted String
        ));

        sendAndExpectValid(detectionAsString);
    }

    @Test
    void shouldFailWhenBatchSizeExceedsLimit() throws Exception {
        List<APIModelDTO> hugeList = java.util.Collections.nCopies(1001, new APIModelDTO());

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hugeList)))
                .andExpect(status().isPayloadTooLarge());
    }

    private void sendAndExpectValid(RequestDTO detection) throws Exception {
        mockMvc.perform(post("/api/detection/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detection)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}