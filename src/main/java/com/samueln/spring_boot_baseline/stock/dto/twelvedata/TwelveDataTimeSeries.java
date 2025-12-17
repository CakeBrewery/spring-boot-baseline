package com.samueln.spring_boot_baseline.stock.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TwelveDataTimeSeries(
        @JsonProperty("meta") Meta meta,
        @JsonProperty("values") List<TimeSeriesValue> values) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("interval") String interval,
            @JsonProperty("currency") String currency,
            @JsonProperty("exchange_timezone") String exchangeTimezone,
            @JsonProperty("exchange") String exchange,
            @JsonProperty("type") String type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TimeSeriesValue(
            @JsonProperty("datetime") String datetime,
            @JsonProperty("open") String open,
            @JsonProperty("high") String high,
            @JsonProperty("low") String low,
            @JsonProperty("close") String close,
            @JsonProperty("volume") String volume) {
    }
}
