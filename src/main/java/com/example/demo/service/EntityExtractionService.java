package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.EntityExtractionResponse;
import com.example.demo.mapper.NlpCloudMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EntityExtractionService extends BaseNlpCloudService {

    private final NlpCloudMapper mapper;

    public EntityExtractionService(RestTemplate rt, NlpCloudProperties props, NlpCloudMapper mapper) {
        super(rt, props);
        this.mapper = mapper;
    }

    public EntityExtractionResponse extractEntities(ClinicalNoteRequest request) {
        String path = "/" + properties.getEntityModel()
                + properties.getEntityEndpoint();

        return executeWithRetry(() -> {
            var payload = Map.of("text", request.getNote());

            String response = restTemplate.postForObject(
                    path, buildRequest(payload), String.class
            );

            return mapper.toEntityExtractionResponse(response);
        });
    }
}