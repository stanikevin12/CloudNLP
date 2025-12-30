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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MedicalNlpResilienceIntegrationTest {

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
    void grammarEndpointReturnsControlledErrorOnTimeout() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/bart-large-cnn/summarization"))
                .willReturn(aResponse()
                        .withFixedDelay(1000)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        ClinicalNoteRequest request = new ClinicalNoteRequest(
                "Patient note experiencing slow upstream response for testing.", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.message").value("Unable to process NLP request at this time. Please try again later."))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."));

        verify(postRequestedFor(urlEqualTo("/v1/bart-large-cnn/summarization")));
    }

    @Test
    void grammarEndpointReturnsControlledErrorOn5xx() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/bart-large-cnn/summarization"))
                .willReturn(aResponse()
                        .withStatus(502)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"temporary\"}")));

        ClinicalNoteRequest request = new ClinicalNoteRequest(
                "Patient note that should trigger upstream failure handling.", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.message").value("Unable to process NLP request at this time. Please try again later."))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."));

        verify(postRequestedFor(urlEqualTo("/v1/bart-large-cnn/summarization")));
    }

    @Test
    void grammarEndpointRetriesAndFailsCleanlyAfterTwoAttempts() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/bart-large-cnn/summarization"))
                .inScenario("retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("second"));
        wireMockServer.stubFor(post(urlEqualTo("/v1/bart-large-cnn/summarization"))
                .inScenario("retry")
                .whenScenarioStateIs("second")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("third"));
        wireMockServer.stubFor(post(urlEqualTo("/v1/bart-large-cnn/summarization"))
                .inScenario("retry")
                .whenScenarioStateIs("third")
                .willReturn(aResponse().withStatus(500)));

        ClinicalNoteRequest request = new ClinicalNoteRequest(
                "Patient note used to verify retry logic with sufficient length.", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.message").value("Unable to process NLP request at this time. Please try again later."))
                .andExpect(jsonPath("$.medicalDisclaimer").value("This tool does not provide diagnosis."));

        assertThat(wireMockServer.getAllServeEvents()).hasSize(3);
        verify(3, postRequestedFor(urlEqualTo("/v1/bart-large-cnn/summarization")));
    }
}
