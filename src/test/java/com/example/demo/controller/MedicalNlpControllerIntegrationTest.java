package com.example.demo.controller;

import com.example.demo.dto.ClinicalNoteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalNlpControllerIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("nlpcloud.api-key", () -> "test-key");
        registry.add("nlpcloud.model", () -> "mock-model");
        registry.add("nlpcloud.timeout", () -> "2s");
        registry.add("nlpcloud.max-retries", () -> "0");
        registry.add("nlpcloud.base-url", () -> mockWebServer.url("/v1").toString());
    }

    @BeforeAll
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterAll
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void grammarEndpointReturnsSuggestions() throws Exception {
        enqueueFixture("src/test/resources/fixtures/grammar.json");
        ClinicalNoteRequest request = new ClinicalNoteRequest("Ths is a bad sentance", "BP 130/90");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correctedText").value("This is the corrected text."))
                .andExpect(jsonPath("$.data.suggestions[0].type").value("spelling"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).contains("/grammar");
    }

    @Test
    void entitiesEndpointReturnsEntities() throws Exception {
        enqueueFixture("src/test/resources/fixtures/entities.json");
        ClinicalNoteRequest request = new ClinicalNoteRequest("John Doe was in Paris", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.entities[0].text").value("John Doe"))
                .andExpect(jsonPath("$.data.entities[1].entity").value("location"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).contains("/entities");
    }

    @Test
    void summarizeEndpointReturnsSummary() throws Exception {
        enqueueFixture("src/test/resources/fixtures/summary.json");
        ClinicalNoteRequest request = new ClinicalNoteRequest("Patient presents...", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary").value("This is a concise summary of the report."))
                .andExpect(jsonPath("$.data.keyFindings[1]").value("Finding two"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).contains("/summarize");
    }

    @Test
    void keywordsEndpointReturnsKeywords() throws Exception {
        enqueueFixture("src/test/resources/fixtures/keywords.json");
        ClinicalNoteRequest request = new ClinicalNoteRequest("AI in healthcare", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/keywords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keywords[0]").value("nlp"));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).contains("/keywords");
    }

    @Test
    void analyzeEndpointReturnsClassification() throws Exception {
        enqueueFixture("src/test/resources/fixtures/classification.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/analyze")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("text", "Space travel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.labels[0]").value("space"))
                .andExpect(jsonPath("$.data.scores[0]").value(0.83));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).contains("/classification");
    }

    private void enqueueFixture(String path) throws IOException {
        String body = Files.readString(Path.of(path));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }
}
