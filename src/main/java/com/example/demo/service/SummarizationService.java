package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.SummaryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.example.demo.validation.NlpInputValidator.validateSummarization;

@Service
public class SummarizationService extends BaseNlpCloudService {

    public SummarizationService(
            @Qualifier("nlpCloudRestTemplate") RestTemplate restTemplate,
            NlpCloudProperties properties
    ) {
        super(restTemplate, properties);
    }

    public SummaryResponse summarize(ClinicalNoteRequest request) {

        // 1️⃣ Validate input size BEFORE calling NLP Cloud
        validateSummarization(request.getNote());

        // 2️⃣ Build relative path (base URL already contains /v1)
        String path = "/" + properties.getSummarizationModel()
                + properties.getSummarizationEndpoint();

        // 3️⃣ Execute NLP Cloud call with safe retry semantics
        return executeWithRetry(() -> {

            Map<String, Object> payload = Map.of(
                    "text", request.getNote(),
                    "size", "small"
            );

            String response = restTemplate.postForObject(
                    path,
                    buildRequest(payload),
                    String.class
            );

            return new SummaryResponse(response);
        });
    }
}
