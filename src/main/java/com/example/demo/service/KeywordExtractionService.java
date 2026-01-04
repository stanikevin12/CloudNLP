package com.example.demo.service;

import com.example.demo.config.NlpCloudClientConfig;
import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.KeywordResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

@Service
public class KeywordExtractionService extends BaseNlpCloudService {
    private static final Logger log = LoggerFactory.getLogger(NlpCloudClientConfig.class);

    public KeywordExtractionService(RestTemplate rt, NlpCloudProperties props) {
        super(rt, props);
    }

    public KeywordResponse extractKeywords(ClinicalNoteRequest request) {


        String path = "/" + properties.getKeywordModel()
                + properties.getKeywordEndpoint();

        log.debug("Keyword model: {}", properties.getKeywordModel());
        log.debug("Keyword endpoint: {}", properties.getKeywordEndpoint());
        log.debug("Full keyword path: {}", path);
        return executeWithRetry(() -> {
            var payload = Map.of("text", request.getNote());

            String response = restTemplate.postForObject(
                    path, buildRequest(payload), String.class
            );

            return new KeywordResponse(List.of(response.split(",")));
        });
    }
}