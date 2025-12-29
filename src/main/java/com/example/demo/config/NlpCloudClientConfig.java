package com.example.demo.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class NlpCloudClientConfig {

    @Bean
    public WebClient nlpCloudWebClient(NlpCloudProperties properties, WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getTimeout().toMillis())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS)))
                .responseTimeout(properties.getTimeout());

        return builder
                .baseUrl(properties.getBaseUrl() + "/" + properties.getModel())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Token " + properties.getApiKey())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
