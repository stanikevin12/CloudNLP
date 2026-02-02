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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MedicalNlpResilienceIntegrationTest {

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
        registry.add("nlpcloud.timeout", () -> "250ms");
        registry.add("nlpcloud.max-retries", () -> "2");
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void grammarEndpointRetriesAndFailsCleanly() throws Exception {
        wireMockServer.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.post(
                        urlEqualTo("/v1/gpu/chatdolphin/gs-correction")
                ).willReturn(
                        aResponse().withStatus(500)
                )
        );

        ClinicalNoteRequest request =
                new ClinicalNoteRequest("Patient note long enough to retry.", null);

        mockMvc.perform(post("/api/nlp/grammar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.message")
                        .value("NLP Cloud request failed with status 500"));

        wireMockServer.verify(
                3,
                postRequestedFor(urlEqualTo("/v1/gpu/chatdolphin/gs-correction"))
        );
    }
}
