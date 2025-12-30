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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalNlpResilienceIntegrationTest {

    private static final MockWebServer mockWebServer;

    static {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start MockWebServer", e);
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("nlpcloud.api-key", () -> "test-key");
        registry.add("nlpcloud.summarization-model", () -> "bart-large-cnn");
        registry.add("nlpcloud.entities-model", () -> "mock-entities-model");
        registry.add("nlpcloud.classification-model", () -> "mock-classification-model");
        registry.add("nlpcloud.timeout", () -> "250ms");
        registry.add("nlpcloud.max-retries", () -> "2");
        registry.add("nlpcloud.base-url", () -> mockWebServer.url("/v1").toString());
    }

    @BeforeAll
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterAll
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void grammarEndpointReturnsControlledErrorOnTimeout() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .setBodyDelay(1, TimeUnit.SECONDS)
                .setHeader("Content-Type", "application/json"));

        ClinicalNoteRequest request = new ClinicalNoteRequest(
                "Patient note experiencing slow upstream response for testing.", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.message").value("Unable to process NLP request at this time. Please try again later."))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/bart-large-cnn/summarization");
    }

    @Test
    void grammarEndpointReturnsControlledErrorOn5xx() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(502)
                .setBody("{\"error\":\"temporary\"}")
                .setHeader("Content-Type", "application/json"));

        ClinicalNoteRequest request = new ClinicalNoteRequest(
                "Patient note that should trigger upstream failure handling.", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.message").value("Unable to process NLP request at this time. Please try again later."))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/bart-large-cnn/summarization");
    }

    @Test
    void grammarEndpointRetriesAndFailsCleanlyAfterTwoAttempts() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        ClinicalNoteRequest request = new ClinicalNoteRequest(
                "Patient note used to verify retry logic with sufficient length.", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.message").value("Unable to process NLP request at this time. Please try again later."))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."));

        RecordedRequest first = mockWebServer.takeRequest();
        RecordedRequest second = mockWebServer.takeRequest();
        RecordedRequest third = mockWebServer.takeRequest();

        assertThat(first.getPath()).isEqualTo("/v1/bart-large-cnn/summarization");
        assertThat(second.getPath()).isEqualTo("/v1/bart-large-cnn/summarization");
        assertThat(third.getPath()).isEqualTo("/v1/bart-large-cnn/summarization");
    }
}

