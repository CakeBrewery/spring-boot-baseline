package com.samueln.spring_boot_baseline.stock.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TwelveDataProfile(
        @JsonProperty("symbol") String symbol,
        @JsonProperty("name") String name,
        @JsonProperty("exchange") String exchange,
        @JsonProperty("sector") String sector,
        @JsonProperty("industry") String industry,
        @JsonProperty("description") String description,
        @JsonProperty("website") String website,
        @JsonProperty("country") String country,
        @JsonProperty("currency") String currency,
        @JsonProperty("market_cap") String marketCap) {
}
