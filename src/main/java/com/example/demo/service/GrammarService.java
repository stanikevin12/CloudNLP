package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.GrammarResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import static com.example.demo.validation.NlpInputValidator.validateGrammar;

@Service
public class GrammarService extends BaseNlpCloudService {

    private static final Logger log = LoggerFactory.getLogger(GrammarService.class);

    public GrammarService(
            @Qualifier("nlpCloudRestTemplate") RestTemplate rt,
            NlpCloudProperties props
    ) {
        super(rt, props);
    }

    public GrammarResponse checkGrammar(ClinicalNoteRequest request) {

        validateGrammar(request.getNote());

        String path = "/gpu/" + properties.getGrammarModel()
                + properties.getGrammarEndpoint();

        log.debug("Grammar NLP Cloud path → {}", path);

        return executeWithRetry(() -> {
            var payload = Map.of("text", request.getNote());

            String response = restTemplate.postForObject(
                    path,
                    buildRequest(payload),
                    String.class
            );

            return new GrammarResponse(response, List.of());
        });
    }
}
