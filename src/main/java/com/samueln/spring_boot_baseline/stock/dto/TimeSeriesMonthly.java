package com.samueln.spring_boot_baseline.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Map;

public record TimeSeriesMonthly(
        @JsonAlias("Meta Data") MetaData metaData,
        @JsonAlias("Monthly Time Series") Map<String, MonthlyData> monthlyTimeSeries) {
}
