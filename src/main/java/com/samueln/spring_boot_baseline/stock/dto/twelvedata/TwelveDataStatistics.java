package com.samueln.spring_boot_baseline.stock.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TwelveDataStatistics(
        @JsonProperty("statistics") StatisticsData statistics) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatisticsData(
            @JsonProperty("valuations_metrics") Map<String, String> valuationsMetrics,
            @JsonProperty("financials") Map<String, String> financials,
            @JsonProperty("stock_price_summary") Map<String, String> stockPriceSummary) {
    }
}
