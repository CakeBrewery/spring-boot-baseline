package com.samueln.spring_boot_baseline.stock;

import com.samueln.spring_boot_baseline.stock.dto.StockSummary;
import com.samueln.spring_boot_baseline.stock.dto.twelvedata.TwelveDataProfile;
import com.samueln.spring_boot_baseline.stock.dto.twelvedata.TwelveDataQuote;
import com.samueln.spring_boot_baseline.stock.dto.twelvedata.TwelveDataTimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;

    public StockService(RestClient.Builder restClientBuilder,
            @Value("${twelvedata.api.key}") String apiKey,
            @Value("${twelvedata.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public StockSummary getStockSummary(String symbol) {
        String normalizedSymbol = symbol == null ? "" : symbol.trim().toUpperCase(Locale.US);

        try {
            TwelveDataQuote quote = fetchQuote(normalizedSymbol);
            TwelveDataProfile profile = fetchProfile(normalizedSymbol);
            TwelveDataTimeSeries timeSeries = fetchTimeSeries(normalizedSymbol);

            List<StockSummary.PricePoint> priceSeries = buildPriceSeries(timeSeries);
            double yearStartPrice = priceSeries.isEmpty() ? 0 : priceSeries.get(0).value();
            double week52High = priceSeries.stream().mapToDouble(StockSummary.PricePoint::value).max().orElse(0);
            double week52Low = priceSeries.stream().mapToDouble(StockSummary.PricePoint::value).min().orElse(0);

            return new StockSummary(
                    normalizedSymbol,
                    coalesce(profile != null ? profile.name() : null, quote != null ? quote.name() : null, "N/A"),
                    coalesce(quote != null ? quote.exchange() : null, profile != null ? profile.exchange() : null,
                            "N/A"),
                    profile != null ? coalesce(profile.sector(), "N/A") : "N/A",
                    "1Y",
                    parseDouble(quote != null ? quote.close() : null),
                    parseDouble(quote != null ? quote.change() : null),
                    parseDouble(quote != null ? quote.percentChange() : null),
                    parseDouble(profile != null ? profile.marketCap() : null),
                    week52High,
                    week52Low,
                    yearStartPrice,
                    profile != null ? coalesce(profile.description(), "") : "",
                    priceSeries);
        } catch (Exception e) {
            logger.error("Error building stock summary for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to build stock summary for symbol: " + symbol, e);
        }
    }

    private TwelveDataQuote fetchQuote(String symbol) {
        String url = String.format("%s/quote?symbol=%s&apikey=%s", baseUrl, symbol, apiKey);
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(TwelveDataQuote.class);
    }

    private TwelveDataProfile fetchProfile(String symbol) {
        String url = String.format("%s/profile?symbol=%s&apikey=%s", baseUrl, symbol, apiKey);
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(TwelveDataProfile.class);
    }

    private TwelveDataTimeSeries fetchTimeSeries(String symbol) {
        String url = String.format("%s/time_series?symbol=%s&interval=1month&outputsize=15&apikey=%s", baseUrl, symbol,
                apiKey);
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(TwelveDataTimeSeries.class);
    }

    private List<StockSummary.PricePoint> buildPriceSeries(TwelveDataTimeSeries response) {
        if (response == null || response.values() == null || response.values().isEmpty()) {
            return List.of();
        }

        List<StockSummary.PricePoint> sortedPoints = response.values().stream()
                .sorted(Comparator.comparing(TwelveDataTimeSeries.TimeSeriesValue::datetime))
                .map(value -> new StockSummary.PricePoint(formatMonthLabel(value.datetime()),
                        parseDouble(value.close())))
                .filter(point -> point.value() > 0)
                .collect(Collectors.toList());

        int startIndex = Math.max(sortedPoints.size() - 12, 0);
        return new ArrayList<>(sortedPoints.subList(startIndex, sortedPoints.size()));
    }

    private String formatMonthLabel(String date) {
        if (date == null || date.isBlank()) {
            return "";
        }
        try {
            LocalDate parsed = LocalDate.parse(date.substring(0, Math.min(date.length(), 10)));
            return parsed.getMonth().getDisplayName(TextStyle.SHORT, Locale.US);
        } catch (DateTimeParseException ex) {
            logger.warn("Unable to parse date {} for price series label", date);
            return date;
        }
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("Unable to parse numeric value: {}", value);
            return 0;
        }
    }

    private String coalesce(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
