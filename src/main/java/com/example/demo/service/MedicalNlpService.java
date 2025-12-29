package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.UpstreamServiceException;
import com.example.demo.mapper.NlpCloudMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;

@Service
public class MedicalNlpService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final NlpCloudMapper mapper;

    @Value("${nlpcloud.api.key}")
    private String apiKey;

    @Value("${nlpcloud.model}")
    private String model;

    public MedicalNlpService(NlpCloudMapper mapper) {
        this.mapper = mapper;
    }

    public GrammarResponse checkGrammar(ClinicalNoteRequest request) {
        return postForResponse("/grammar", request, mapper::toGrammarResponse);
    }

    public EntityExtractionResponse extractEntities(ClinicalNoteRequest request) {
        return postForResponse("/entities", request, mapper::toEntityExtractionResponse);
    }

    public SummaryResponse summarize(ClinicalNoteRequest request) {
        return postForResponse("/summarize", request, mapper::toSummaryResponse);
    }

    public KeywordResponse keywords(ClinicalNoteRequest request) {
        return postForResponse("/keywords", request, mapper::toKeywordResponse);
    }

    private <T> T postForResponse(String path,
                                  ClinicalNoteRequest request,
                                  Function<String, T> mapperFunction) {
        String url = "https://api.nlpcloud.io/v1/" + model + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + apiKey);

        Map<String, String> payload = new HashMap<>();
        payload.put("text", request.getNote());
        if (request.getPatientContext() != null && !request.getPatientContext().isEmpty()) {
            payload.put("context", request.getPatientContext());
        }

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            if (response.getBody() == null) {
                throw new UpstreamServiceException("Upstream NLP service returned an empty response");
            }
            return mapperFunction.apply(response.getBody());
        } catch (RestClientResponseException e) {
            throw new UpstreamServiceException("Upstream service responded with status " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new UpstreamServiceException("Failed to reach upstream NLP service", e);
        } catch (IllegalArgumentException e) {
            throw new UpstreamServiceException("Unable to parse upstream NLP response", e);
        }
    }
}
