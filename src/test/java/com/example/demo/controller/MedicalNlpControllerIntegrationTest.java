package com.example.demo.controller;

import com.example.demo.dto.ClinicalNoteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalNlpControllerIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        WireMockServer server = ensureWireMockServer();
        registry.add("nlpcloud.base-url", server::baseUrl);
    }

    @BeforeAll
    void setup() {
        ensureWireMockServer();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void resetWireMock() {
        reset();
    }

    @AfterAll
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private static WireMockServer ensureWireMockServer() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(options().dynamicPort());
            wireMockServer.start();
            configureFor(wireMockServer.port());
        }
        return wireMockServer;
    }

    @Test
    void grammarEndpointReturnsSuggestions() throws Exception {
        enqueueFixture("src/test/resources/fixtures/grammar.json");
        ClinicalNoteRequest request = new ClinicalNoteRequest("Ths is a bad sentance", "BP 130/90");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/api/nlp/grammar"))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.correctedText").isNotEmpty());

        verify(postRequestedFor(urlEqualTo("/v1/bart-large-cnn/summarization")));
    }

    @Test
    void entitiesEndpointReturnsEntities() throws Exception {
        enqueueFixture("src/test/resources/fixtures/entities.json");
        ClinicalNoteRequest request = new ClinicalNoteRequest("John Doe was in Paris", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/api/nlp/entities"))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.entities[0].text").value("John Doe"))
                .andExpect(jsonPath("$.data.entities[1].entity").value("location"));

        verify(postRequestedFor(urlEqualTo("/v1/bart-large-cnn/summarization")));
    }

    @Test
    void summarizeEndpointReturnsSummary() throws Exception {
        enqueueFixture("src/test/resources/fixtures/summary.json");
        ClinicalNoteRequest request = new ClinicalNoteRequest(
                "Patient presents with chest discomfort and dizziness today.", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/api/nlp/summarize"))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.summary").value("This is a concise summary of the report."))
                .andExpect(jsonPath("$.data.keyFindings[1]").value("Finding two"));

        verify(postRequestedFor(urlEqualTo("/v1/bart-large-cnn/summarization")));
    }

    @Test
    void keywordsEndpointReturnsKeywords() throws Exception {
        enqueueFixture("src/test/resources/fixtures/keywords.json");
        ClinicalNoteRequest request = new ClinicalNoteRequest(
                "AI in healthcare for clinical decision support systems.", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/keywords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.path").value("/api/nlp/keywords"))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.keywords[0]").isNotEmpty());

        verify(postRequestedFor(urlEqualTo("/v1/bart-large-cnn/summarization")));
    }

    private void enqueueFixture(String path) throws IOException {
        String body = Files.readString(Path.of(path));
        wireMockServer.stubFor(post(urlEqualTo("/v1/bart-large-cnn/summarization"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }
}
