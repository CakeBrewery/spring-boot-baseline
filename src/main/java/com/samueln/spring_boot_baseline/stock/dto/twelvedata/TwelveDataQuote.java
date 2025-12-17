package com.samueln.spring_boot_baseline.stock.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TwelveDataQuote(
        @JsonProperty("symbol") String symbol,
        @JsonProperty("name") String name,
        @JsonProperty("exchange") String exchange,
        @JsonProperty("currency") String currency,
        @JsonProperty("datetime") String datetime,
        @JsonProperty("open") String open,
        @JsonProperty("high") String high,
        @JsonProperty("low") String low,
        @JsonProperty("close") String close,
        @JsonProperty("volume") String volume,
        @JsonProperty("previous_close") String previousClose,
        @JsonProperty("change") String change,
        @JsonProperty("percent_change") String percentChange) {
}
