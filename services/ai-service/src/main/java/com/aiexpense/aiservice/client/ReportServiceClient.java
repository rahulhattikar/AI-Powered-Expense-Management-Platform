package com.aiexpense.aiservice.client;

import com.aiexpense.aiservice.dto.MonthlySummaryResponse;
import com.aiexpense.aiservice.exception.ReportServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReportServiceClient {

    private final RestClient reportServiceRestClient;

    public MonthlySummaryResponse getMonthlySummary(int month, int year, String authorizationHeader) {
        try {
            return reportServiceRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/reports/monthly-summary")
                            .queryParam("month", month)
                            .queryParam("year", year)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(MonthlySummaryResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch monthly summary from report service - month: {} year: {} - {}",
                    month, year, e.getMessage());
            throw new ReportServiceUnavailableException("Unable to reach report service", e);
        }
    }
}
