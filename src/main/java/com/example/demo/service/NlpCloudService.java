package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClassificationRequest;
import com.example.demo.dto.ClassificationResponse;
import com.example.demo.exception.UpstreamServiceException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
public class NlpCloudService {

    private final WebClient nlpCloudWebClient;
    private final NlpCloudProperties properties;

    public NlpCloudService(WebClient nlpCloudWebClient, NlpCloudProperties properties) {
        this.nlpCloudWebClient = nlpCloudWebClient;
        this.properties = properties;
    }

    public ClassificationResponse classify(String text) {

        ClassificationRequest requestBody = new ClassificationRequest(
                text,
                List.of("space", "sport", "business", "journalism", "politics"),
                true
        );

        Mono<ClassificationResponse> responseMono = nlpCloudWebClient.post()
                .uri("/classification")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ClassificationResponse.class)
                .timeout(properties.getTimeout())
                .retryWhen(buildRetrySpec())
                .onErrorMap(this::mapUpstreamError);

        return responseMono.block();
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
