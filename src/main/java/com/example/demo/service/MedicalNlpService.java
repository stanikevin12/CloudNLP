package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.*;
import com.example.demo.exception.UpstreamServiceException;
import com.example.demo.mapper.NlpCloudMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@Service
public class MedicalNlpService {

    private final WebClient nlpCloudWebClient;
    private final NlpCloudMapper mapper;
    private final NlpCloudProperties properties;

    public MedicalNlpService(WebClient nlpCloudWebClient, NlpCloudMapper mapper, NlpCloudProperties properties) {
        this.nlpCloudWebClient = nlpCloudWebClient;
        this.mapper = mapper;
        this.properties = properties;
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
        Map<String, String> payload = new HashMap<>();
        payload.put("text", request.getNote());
        if (request.getPatientContext() != null && !request.getPatientContext().isEmpty()) {
            payload.put("context", request.getPatientContext());
        }

        Mono<String> responseMono = nlpCloudWebClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(properties.getTimeout())
                .retryWhen(buildRetrySpec())
                .onErrorMap(this::mapUpstreamError);

        return mapperFunction.apply(responseMono.block());
    }

    private Retry buildRetrySpec() {
        return Retry.backoff(properties.getMaxRetries(), Duration.ofMillis(300))
                .filter(this::isRetryable)
                .onRetryExhaustedThrow((spec, signal) ->
                        new UpstreamServiceException("Upstream NLP service did not respond after retries", signal.failure()));
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof TimeoutException || throwable instanceof IOException) {
            return true;
        }
        if (throwable instanceof WebClientResponseException responseException) {
            return responseException.getStatusCode().is5xxServerError();
        }
        return false;
    }

    private Throwable mapUpstreamError(Throwable throwable) {
        if (throwable instanceof UpstreamServiceException) {
            return throwable;
        }
        if (throwable instanceof WebClientResponseException responseException) {
            return new UpstreamServiceException(
                    "Upstream service responded with status " + responseException.getStatusCode().value(),
                    responseException);
        }
        if (throwable instanceof TimeoutException) {
            return new UpstreamServiceException("Timed out calling upstream NLP service", throwable);
        }
        return new UpstreamServiceException("Failed to reach upstream NLP service", throwable);
    }
}
