package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.SummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SummarizationService extends BaseNlpCloudService {

    public SummarizationService(RestTemplate rt, NlpCloudProperties props) {
        super(rt, props);
    }

    public SummaryResponse summarize(ClinicalNoteRequest request) {
        String path = "/" + properties.getSummarizationModel()
                + properties.getSummarizationEndpoint();

        return executeWithRetry(() -> {
            var payload = Map.of(
                    "text", request.getNote(),
                    "size", "small"
            );

            var response = restTemplate.postForObject(
                    path, buildRequest(payload), String.class
            );

            return new SummaryResponse(response);
        });
    }
}