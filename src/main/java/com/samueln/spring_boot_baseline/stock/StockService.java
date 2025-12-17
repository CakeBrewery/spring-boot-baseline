package com.samueln.spring_boot_baseline.stock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samueln.spring_boot_baseline.stock.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;

    public StockService(RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${alphavantage.api.key}") String apiKey,
            @Value("${alphavantage.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public GlobalQuote getGlobalQuote(String symbol) {
        String jsonResponse = restClient.get()
                .uri(baseUrl + "?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apiKey}", symbol, apiKey)
                .retrieve()
                .body(String.class);

        validateResponse(jsonResponse);

        try {
            GlobalQuoteResponse response = objectMapper.readValue(jsonResponse, GlobalQuoteResponse.class);
            if (response != null && response.globalQuote() != null) {
                return response.globalQuote();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse global quote response", e);
        }
        throw new RuntimeException("Failed to fetch global quote for symbol: " + symbol);
    }

    public CompanyOverview getCompanyOverview(String symbol) {
        String jsonResponse = restClient.get()
                .uri(baseUrl + "?function=OVERVIEW&symbol={symbol}&apikey={apiKey}", symbol, apiKey)
                .retrieve()
                .body(String.class);

        validateResponse(jsonResponse);

        try {
            CompanyOverview response = objectMapper.readValue(jsonResponse, CompanyOverview.class);
            if (response != null && response.symbol() != null) {
                return response;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse company overview response", e);
        }
        throw new RuntimeException("Failed to fetch company overview for symbol: " + symbol);
    }

    public TimeSeriesMonthly getMonthlyTimeSeries(String symbol) {
        String jsonResponse = restClient.get()
                .uri(baseUrl + "?function=TIME_SERIES_MONTHLY&symbol={symbol}&apikey={apiKey}", symbol, apiKey)
                .retrieve()
                .body(String.class);

        validateResponse(jsonResponse);

        try {
            TimeSeriesMonthly response = objectMapper.readValue(jsonResponse, TimeSeriesMonthly.class);
            if (response != null && response.monthlyTimeSeries() != null) {
                return response;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse monthly time series response", e);
        }
        throw new RuntimeException("Failed to fetch monthly time series for symbol: " + symbol);
    }

    private void validateResponse(String jsonResponse) {
        if (jsonResponse == null) {
            throw new RuntimeException("Received empty response from AlphaVantage API");
        }
        // AlphaVantage returns "Information" or "Note" when rate limits are hit
        if (jsonResponse.contains("\"Information\"") || jsonResponse.contains("\"Note\"")) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "AlphaVantage API rate limit reached. Please wait and try again.");
        }
        if (jsonResponse.contains("\"Error Message\"")) {
            throw new RuntimeException("AlphaVantage API Error: " + jsonResponse);
        }
    }
}
