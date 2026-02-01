package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.EntityExtractionResponse;
import com.example.demo.mapper.NlpCloudMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.example.demo.validation.NlpInputValidator.validateEntities;

@Service
public class EntityExtractionService extends BaseNlpCloudService {

    private static final Logger log =
            LoggerFactory.getLogger(EntityExtractionService.class);

    private final NlpCloudMapper mapper;

    public EntityExtractionService(
            @Qualifier("nlpCloudRestTemplate") RestTemplate rt,
            NlpCloudProperties props,
            NlpCloudMapper mapper
    ) {
        super(rt, props);
        this.mapper = mapper;
    }

    public EntityExtractionResponse extractEntities(ClinicalNoteRequest request) {

        validateEntities(request.getNote());

        String path = "/" + properties.getEntityModel()
                + properties.getEntityEndpoint();

        log.debug("Entity NLP Cloud path → {}", path);

        return executeWithRetry(() -> {
            var payload = Map.of("text", request.getNote());

            String response = restTemplate.postForObject(
                    path,
                    buildRequest(payload),
                    String.class
            );

            return mapper.toEntityExtractionResponse(response);
        });
    }
}
