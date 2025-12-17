package com.samueln.spring_boot_baseline.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GlobalQuoteResponse(
        @JsonAlias("Global Quote") GlobalQuote globalQuote) {
}
