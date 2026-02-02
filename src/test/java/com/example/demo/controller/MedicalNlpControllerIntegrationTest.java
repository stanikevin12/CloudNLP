package com.example.demo.controller;

import com.example.demo.dto.ClinicalNoteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MedicalNlpControllerIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    static {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("nlpcloud.api-key", () -> "test-key");
        registry.add("nlpcloud.base-url", () -> wireMockServer.baseUrl());
        registry.add("nlpcloud.timeout", () -> "2s");
        registry.add("nlpcloud.max-retries", () -> "0");
    }

    @AfterEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void grammarEndpointReturnsSuggestions() throws Exception {
        stub("/v1/gpu/chatdolphin/gs-correction", "fixtures/grammar.json");

        ClinicalNoteRequest request =
                new ClinicalNoteRequest("Ths is a bad sentance", "BP 130/90");

        mockMvc.perform(post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.correctedText").isNotEmpty());

        wireMockServer.verify(
                postRequestedFor(urlEqualTo("/v1/gpu/chatdolphin/gs-correction"))
        );
    }

    @Test
    void entitiesEndpointReturnsEntities() throws Exception {
        stub("/v1/en_core_web_lg/entities", "fixtures/entities.json");

        ClinicalNoteRequest request =
                new ClinicalNoteRequest("John Doe was in Paris", null);

        mockMvc.perform(post("/api/nlp/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.entities[0].text").value("John Doe"))
                .andExpect(jsonPath("$.data.entities[1].entity").value("location"));

        wireMockServer.verify(
                postRequestedFor(urlEqualTo("/v1/en_core_web_lg/entities"))
        );
    }

    @Test
    void summarizeEndpointReturnsSummary() throws Exception {
        stub("/v1/bart-large-cnn/summarization", "fixtures/summary.json");

        ClinicalNoteRequest request =
                new ClinicalNoteRequest("Patient presents with chest pain.", null);

       mockMvc.perform(post("/api/nlp/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.error").value(nullValue()))
        .andExpect(jsonPath("$.data.summary")
                .value(org.hamcrest.Matchers.containsString(
                        "This is a concise summary of the report."
                )))
        .andExpect(jsonPath("$.data.summary")
                .value(org.hamcrest.Matchers.containsString(
                        "Finding two"
                )));
            }


    @Test
    void keywordsEndpointReturnsKeywords() throws Exception {
        stub("/v1/gpu/llama-3-1-405b/kw-kp-extraction", "fixtures/keywords.json");

        ClinicalNoteRequest request =
                new ClinicalNoteRequest("Artificial intelligence in healthcare enables clinical decision support systems.", null);

        mockMvc.perform(post("/api/nlp/keywords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keywords").isArray());

        wireMockServer.verify(
                postRequestedFor(urlEqualTo("/v1/gpu/llama-3-1-405b/kw-kp-extraction"))
        );
    }

    private void stub(String endpoint, String fixture) throws IOException {
        String body = Files.readString(Path.of("src/test/resources/" + fixture));

        wireMockServer.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.post(
                        urlEqualTo(endpoint)
                ).willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(body)
                )
        );
    }
}
