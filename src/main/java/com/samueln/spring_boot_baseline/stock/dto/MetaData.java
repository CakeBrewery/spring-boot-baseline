package com.samueln.spring_boot_baseline.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record MetaData(
        @JsonAlias("1. Information") String information,
        @JsonAlias("2. Symbol") String symbol,
        @JsonAlias("3. Last Refreshed") String lastRefreshed,
        @JsonAlias("4. Time Zone") String timeZone) {
}
