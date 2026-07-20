package com.aiexpense.aiservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AiClientConfig {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${report.service.url}")
    private String reportServiceUrl;

    @Bean
    public RestClient geminiRestClient() {
        return RestClient.builder()
                .baseUrl(geminiApiUrl)
                .requestFactory(buildRequestFactory(5, 30))
                .build();
    }

    @Bean
    public RestClient reportServiceRestClient() {
        return RestClient.builder()
                .baseUrl(reportServiceUrl)
                .requestFactory(buildRequestFactory(3, 10))
                .build();
    }

    private ClientHttpRequestFactory buildRequestFactory(int connectSeconds, int readSeconds) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(connectSeconds))
                .withReadTimeout(Duration.ofSeconds(readSeconds));
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }
}
