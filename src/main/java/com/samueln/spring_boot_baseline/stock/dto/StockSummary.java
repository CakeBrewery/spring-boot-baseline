package com.samueln.spring_boot_baseline.stock.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record StockSummary(
        String symbol,
        String companyName,
        String exchange,
        String sector,
        String timeline,
        double price,
        double dailyChange,
        double dailyChangePercent,
        double marketCap,
        double week52High,
        double week52Low,
        double yearStartPrice,
        String description,
        List<PricePoint> priceSeries) {

    public record PricePoint(String label, double value) {
    }
}
