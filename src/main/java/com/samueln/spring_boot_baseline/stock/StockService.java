package com.samueln.spring_boot_baseline.stock;

import com.samueln.spring_boot_baseline.stock.dto.*;
import com.samueln.spring_boot_baseline.stock.dto.twelvedata.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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

    public GlobalQuote getGlobalQuote(String symbol) {
        String url = String.format("%s/quote?symbol=%s&apikey=%s", baseUrl, symbol, apiKey);

        try {
            TwelveDataQuote response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(TwelveDataQuote.class);

            if (response == null || response.symbol() == null) {
                throw new RuntimeException("Empty response or missing symbol for Global Quote");
            }

            // Map to existing GlobalQuote DTO
            return new GlobalQuote(
                    response.symbol(),
                    response.open(),
                    response.high(),
                    response.low(),
                    response.close(), // price
                    response.volume(),
                    response.datetime(), // latest trading day
                    response.previousClose(),
                    response.change(),
                    response.percentChange() + "%" // AlphaVantage usually includes %
            );

        } catch (Exception e) {
            logger.error("Error fetching global quote for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to fetch global quote for symbol: " + symbol, e);
        }
    }

    public CompanyOverview getCompanyOverview(String symbol) {
        // Twelve Data requires two calls for this: /profile and /statistics
        String profileUrl = String.format("%s/profile?symbol=%s&apikey=%s", baseUrl, symbol, apiKey);
        String statsUrl = String.format("%s/statistics?symbol=%s&apikey=%s", baseUrl, symbol, apiKey);

        try {
            TwelveDataProfile profile = restClient.get().uri(profileUrl).retrieve().body(TwelveDataProfile.class);
            TwelveDataStatistics stats = restClient.get().uri(statsUrl).retrieve().body(TwelveDataStatistics.class);

            if (profile == null)
                throw new RuntimeException("Failed to fetch profile");

            TwelveDataStatistics.StatisticsData data = (stats != null) ? stats.statistics() : null;
            if (data == null) {
                data = new TwelveDataStatistics.StatisticsData(Collections.emptyMap(), Collections.emptyMap(),
                        Collections.emptyMap());
            }

            Map<String, String> valuations = data.valuationsMetrics();
            if (valuations == null)
                valuations = Collections.emptyMap();

            Map<String, String> financials = data.financials();
            if (financials == null)
                financials = Collections.emptyMap();

            // Safe helper to get from maps

            return new CompanyOverview(
                    profile.symbol(),
                    "Common Stock", // AssetType
                    profile.name(),
                    profile.description(),
                    "N/A", // CIK not provided
                    profile.exchange(),
                    profile.currency(),
                    profile.country(),
                    profile.sector(),
                    profile.industry(),
                    "N/A", // Address
                    profile.website(),
                    "N/A", // FiscalYearEnd
                    "N/A", // LatestQuarter
                    valuations.getOrDefault("market_capitalization", "0"),
                    valuations.getOrDefault("enterprise_value", "0"), // EBITDA approximation or mapping issue? EV is
                                                                      // usually separate. Twelve Data has
                                                                      // enterprise_value.
                    valuations.getOrDefault("pe_ratio", "0"),
                    valuations.getOrDefault("peg_ratio", "0"),
                    financials.getOrDefault("book_value", "0"),
                    valuations.getOrDefault("dividend_yield", "0"), // DividendPerShare approx? No, yield.
                    valuations.getOrDefault("dividend_yield", "0"),
                    valuations.getOrDefault("trailing_eps", "0"), // EPS
                    "0", // RevenuePerShareTTM
                    financials.getOrDefault("profit_margin", "0"),
                    financials.getOrDefault("operating_margin", "0"),
                    financials.getOrDefault("return_on_assets", "0"),
                    financials.getOrDefault("return_on_equity", "0"),
                    financials.getOrDefault("revenue", "0"),
                    financials.getOrDefault("gross_profit", "0"),
                    valuations.getOrDefault("diluted_eps", "0"),
                    "0", // QuarterlyEarningsGrowthYOY
                    "0", // QuarterlyRevenueGrowthYOY
                    "0", // AnalystTargetPrice
                    valuations.getOrDefault("trailing_pe", "0"),
                    valuations.getOrDefault("forward_pe", "0"),
                    valuations.getOrDefault("price_sales_ratio", "0"),
                    valuations.getOrDefault("price_book_ratio", "0"),
                    valuations.getOrDefault("enterprise_value_revenue", "0"),
                    valuations.getOrDefault("enterprise_value_ebitda", "0"),
                    "0", // Beta
                    "0", // 52WeekHigh - usually in quote or separate endpoint
                    "0", // 52WeekLow
                    "0", // 50DayMovingAverage
                    "0", // 200DayMovingAverage
                    "0", // SharesOutstanding
                    "0", // DividendDate
                    "0" // ExDividendDate
            );

        } catch (Exception e) {
            logger.error("Error fetching company overview for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to fetch company overview for symbol: " + symbol, e);
        }
    }

    public TimeSeriesMonthly getMonthlyTimeSeries(String symbol) {
        String url = String.format("%s/time_series?symbol=%s&interval=1month&apikey=%s", baseUrl, symbol, apiKey);

        try {
            TwelveDataTimeSeries response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(TwelveDataTimeSeries.class);

            if (response == null || response.values() == null) {
                throw new RuntimeException("Empty response for Time Series");
            }

            // Convert List to Map
            Map<String, MonthlyData> monthlyDataMap = response.values().stream()
                    .collect(Collectors.toMap(
                            TwelveDataTimeSeries.TimeSeriesValue::datetime,
                            v -> new MonthlyData(v.open(), v.high(), v.low(), v.close(), v.volume()),
                            (v1, v2) -> v1, // merge function (shouldn't be needed for unique dates)
                            LinkedHashMap::new // Preserve order
                    ));

            MetaData meta = new MetaData(
                    "Monthly Prices (open, high, low, close) and Volumes",
                    response.meta().symbol(),
                    "N/A", // Last Refreshed
                    response.meta().exchangeTimezone());

            return new TimeSeriesMonthly(meta, monthlyDataMap);

        } catch (Exception e) {
            logger.error("Error fetching monthly time series for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to fetch monthly time series for symbol: " + symbol, e);
        }
    }
}
