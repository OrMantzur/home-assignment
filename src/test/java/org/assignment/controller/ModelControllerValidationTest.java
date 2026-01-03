package org.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assignment.exception.AppErrorCode;
import org.assignment.model.APIModelDTO;
import org.assignment.model.APIModelParamDTO;
import org.assignment.model.APIModelsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the ModelController focusing on validation scenarios.
 * <p>
 * This test class covers various invalid input scenarios to ensure that
 * the ModelController correctly identifies and responds to malformed requests.
 */
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
@AutoConfigureMockMvc
class ModelControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupLearningPhase() throws Exception {
        // 1. Read JSON from classpath
        ClassPathResource resource = new ClassPathResource("api-models/model_list.json");
        String modelsJson = Files.readString(resource.getFile().toPath());

        // 2. Send to Ingestion API (POST /api/models)
        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modelsJson))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenPathIsMissing() throws Exception {
        // 1. Create Model with Missing Path
        APIModelDTO badModel = new APIModelDTO();
        badModel.setMethod("POST"); // Path is NULL
        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(List.of(badModel));

        // 2. Perform Request
        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiModelsDTO)))
                .andDo(print()) // Prints response to console for debugging
                .andExpect(status().isBadRequest()) // Expect 400
                .andExpect(jsonPath("$.errorCode").value("ERROR-4022")) // INVALID_MODEL_SYNTAX
                // Check that debug message contains the specific error from BindingResult
                .andExpect(jsonPath("$.debugMessage").value(org.hamcrest.Matchers.containsString("Model at index 0 is missing 'path'")));
    }

    @Test
    void shouldFailWhenMethodIsMissing() throws Exception {
        APIModelDTO badModel = new APIModelDTO();
        badModel.setPath("/api/test");
        // Method is NULL

        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(List.of(badModel));

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiModelsDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(AppErrorCode.INVALID_MODEL_SYNTAX.getCode()))
                .andExpect(jsonPath("$.debugMessage").value(org.hamcrest.Matchers.containsString("Model at index 0 is missing 'method'")));
    }

    @Test
    void shouldFailWhenParamNameIsMissing() throws Exception {
        APIModelDTO badModel = new APIModelDTO();
        badModel.setPath("/api/test");
        badModel.setMethod("GET");

        // Param with missing Name
        APIModelParamDTO badParam = new APIModelParamDTO(null, List.of("String"), true);
        badModel.setQueryParams(List.of(badParam));
        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(List.of(badModel));

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiModelsDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERROR-4022"))
                .andExpect(jsonPath("$.debugMessage").value(org.hamcrest.Matchers.containsString("apiModelsDTO[0].queryParams[0] is missing 'name'")));
    }

    @Test
    void shouldFailWhenParamTypesAreEmpty() throws Exception {
        APIModelDTO badModel = new APIModelDTO();
        badModel.setPath("/api/test");
        badModel.setMethod("GET");

        // Param with Name but Empty Types
        APIModelParamDTO badParam = new APIModelParamDTO("userid", List.of(), true);
        badModel.setHeaders(List.of(badParam));
        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(List.of(badModel));

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiModelsDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERROR-4022"))
                .andExpect(jsonPath("$.debugMessage").value(org.hamcrest.Matchers.containsString("At least one type is required")));
    }

    @Test
    void shouldHandleMultipleErrorsInOneResponse() throws Exception {
        // Create 2 bad models
        APIModelDTO model1 = new APIModelDTO(); // Missing Method & Path

        APIModelDTO model2 = new APIModelDTO();
        model2.setPath("/api/ok");
        model2.setMethod("GET");
        model2.setBody(List.of(new APIModelParamDTO("id", null, true))); // Missing Types
        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(List.of(model1, model2));

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiModelsDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.debugMessage").value(org.hamcrest.Matchers.containsString("Model at index 0 is missing 'path'")))
                .andExpect(jsonPath("$.debugMessage").value(org.hamcrest.Matchers.containsString("At least one type is required")));
    }

    @Test
    void shouldHandleInvalidJsonFormat() throws Exception {
        // Sending broken JSON (missing closing brace)
        String brokenJson = "[ { \"method\": \"POST\", \"path\": \"/api/bad\" ";

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(AppErrorCode.INVALID_JSON_FORMAT.getCode()));
    }

    @Test
    void shouldHandle404NotFound() throws Exception {
        // Trying to access a non-existent URL
        mockMvc.perform(get("/api/ghost-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(AppErrorCode.RESOURCE_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("Security: Reject model with excessive number of parameters (DoS protection)")
    void testExceedingMaxParamSize() throws Exception {
        List<APIModelParamDTO> massiveParams = new ArrayList<>();
        // Creating 10,000 parameters to simulate a resource exhaustion attack
        for (int i = 0; i < 10000; i++) {
            massiveParams.add(new APIModelParamDTO("param" + i, List.of("String"), true));
        }

        APIModelDTO massiveModel = new APIModelDTO("/api/heavy", "POST", massiveParams, List.of(), List.of());
        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(List.of(massiveModel));


        // Note: In a real system, we'd expect 413 Payload Too Large or 400 Bad Request
        // If your controller has @Valid with @Size limits, this will fail as expected.
        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiModelsDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Security: Reject unsupported or malicious type names")
    void testInvalidTypeWhitelisting() throws Exception {
        APIModelParamDTO badParam = new APIModelParamDTO(
                "username",
                List.of("NOT_A_REAL_TYPE", "Int"),
                true
        );

        APIModelDTO model = new APIModelDTO("/api/test", "POST", List.of(badParam), List.of(), List.of());
        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(List.of(model));

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiModelsDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Security: Reject excessively long type strings (Buffer protection)")
    void testExceedingMaxTypeSize() throws Exception {
        // A type name that is 5000 characters long
        String hugeType = "String".repeat(1000);

        APIModelParamDTO badParam = new APIModelParamDTO("name", List.of(hugeType), true);
        APIModelDTO model = new APIModelDTO("/api/test", "POST", List.of(badParam), List.of(), List.of());
        APIModelsDTO apiModelsDTO = new APIModelsDTO();
        apiModelsDTO.setApiModelsDTO(List.of(model));

        mockMvc.perform(post("/api/models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiModelsDTO)))
                .andExpect(status().isBadRequest());
    }

}