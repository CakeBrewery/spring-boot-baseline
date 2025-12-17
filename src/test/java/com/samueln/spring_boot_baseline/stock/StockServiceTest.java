package com.samueln.spring_boot_baseline.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samueln.spring_boot_baseline.stock.dto.CompanyOverview;
import com.samueln.spring_boot_baseline.stock.dto.GlobalQuote;
import com.samueln.spring_boot_baseline.stock.dto.TimeSeriesMonthly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StockServiceTest {

    private StockService stockService;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        ObjectMapper objectMapper = new ObjectMapper();
        
        stockService = new StockService(builder, objectMapper, "test-key", "https://api.twelvedata.com");
    }

    @Test
    void shouldGetGlobalQuote() {
        String jsonResponse = """
            {
              "symbol": "AAPL",
              "name": "Apple Inc",
              "exchange": "NASDAQ",
              "currency": "USD",
              "datetime": "2023-10-27",
              "open": "166.91000",
              "high": "168.96000",
              "low": "166.83000",
              "close": "168.22000",
              "volume": "58499129",
              "previous_close": "166.89000",
              "change": "1.33000",
              "percent_change": "0.79693"
            }
            """;

        server.expect(requestTo("https://api.twelvedata.com/quote?symbol=AAPL&apikey=test-key"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        GlobalQuote quote = stockService.getGlobalQuote("AAPL");

        assertThat(quote.symbol()).isEqualTo("AAPL");
        assertThat(quote.price()).isEqualTo("168.22000"); // mapped from close
        assertThat(quote.changePercent()).isEqualTo("0.79693%"); // appended %
    }

    @Test
    void shouldGetCompanyOverview() {
        String profileJson = """
            {
              "symbol": "AAPL",
              "name": "Apple Inc",
              "exchange": "NASDAQ",
              "sector": "Technology",
              "industry": "Consumer Electronics",
              "description": "Apple description",
              "website": "http://www.apple.com",
              "country": "US",
              "currency": "USD"
            }
            """;

        String statsJson = """
            {
              "statistics": {
                "valuations_metrics": {
                  "pe_ratio": "28.5",
                  "market_capitalization": "2000000000"
                },
                "financials": {
                  "price_to_book": "30.1"
                }
              }
            }
            """;

        server.expect(requestTo("https://api.twelvedata.com/profile?symbol=AAPL&apikey=test-key"))
                .andRespond(withSuccess(profileJson, MediaType.APPLICATION_JSON));
        
        server.expect(requestTo("https://api.twelvedata.com/statistics?symbol=AAPL&apikey=test-key"))
                .andRespond(withSuccess(statsJson, MediaType.APPLICATION_JSON));

        CompanyOverview overview = stockService.getCompanyOverview("AAPL");

        assertThat(overview.symbol()).isEqualTo("AAPL");
        assertThat(overview.sector()).isEqualTo("Technology");
        assertThat(overview.peRatio()).isEqualTo("28.5");
        assertThat(overview.marketCapitalization()).isEqualTo("2000000000");
    }

    @Test
    void shouldGetMonthlyTimeSeries() {
        String timeSeriesJson = """
            {
              "meta": {
                "symbol": "AAPL",
                "interval": "1month",
                "currency": "USD",
                "exchange_timezone": "America/New_York",
                "exchange": "NASDAQ",
                "type": "Common Stock"
              },
              "values": [
                {
                  "datetime": "2023-10-27",
                  "open": "171.22000",
                  "high": "182.33999",
                  "low": "166.83000",
                  "close": "168.22000",
                  "volume": "100000"
                }
              ]
            }
            """;

        server.expect(requestTo("https://api.twelvedata.com/time_series?symbol=AAPL&interval=1month&apikey=test-key"))
                .andRespond(withSuccess(timeSeriesJson, MediaType.APPLICATION_JSON));

        TimeSeriesMonthly ts = stockService.getMonthlyTimeSeries("AAPL");

        assertThat(ts.metaData().symbol()).isEqualTo("AAPL");
        assertThat(ts.monthlyTimeSeries()).containsKey("2023-10-27");
        assertThat(ts.monthlyTimeSeries().get("2023-10-27").close()).isEqualTo("168.22000");
    }
}