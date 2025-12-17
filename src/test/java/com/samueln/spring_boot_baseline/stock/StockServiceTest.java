package com.samueln.spring_boot_baseline.stock;

import com.samueln.spring_boot_baseline.stock.dto.StockSummary;
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
        stockService = new StockService(builder, "test-key", "https://api.twelvedata.com");
    }

    @Test
    void shouldBuildStockSummary() {
        String quoteResponse = """
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
                  "currency": "USD",
                  "market_cap": "2500000000000"
                }
                """;

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
                      "datetime": "2023-09-29",
                      "open": "150",
                      "high": "155",
                      "low": "149",
                      "close": "154",
                      "volume": "100000"
                    },
                    {
                      "datetime": "2023-10-27",
                      "open": "166.91000",
                      "high": "168.96000",
                      "low": "166.83000",
                      "close": "168.22000",
                      "volume": "58499129"
                    }
                  ]
                }
                """;

        server.expect(requestTo("https://api.twelvedata.com/quote?symbol=AAPL&apikey=test-key"))
                .andRespond(withSuccess(quoteResponse, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.twelvedata.com/profile?symbol=AAPL&apikey=test-key"))
                .andRespond(withSuccess(profileJson, MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                "https://api.twelvedata.com/time_series?symbol=AAPL&interval=1month&outputsize=15&apikey=test-key"))
                .andRespond(withSuccess(timeSeriesJson, MediaType.APPLICATION_JSON));

        StockSummary summary = stockService.getStockSummary("AAPL");

        assertThat(summary.symbol()).isEqualTo("AAPL");
        assertThat(summary.companyName()).isEqualTo("Apple Inc");
        assertThat(summary.price()).isEqualTo(168.22000);
        assertThat(summary.marketCap()).isEqualTo(2_500_000_000_000d);
        assertThat(summary.priceSeries()).hasSize(2);
        assertThat(summary.week52High()).isEqualTo(168.22000);
        assertThat(summary.week52Low()).isEqualTo(154.0);
        assertThat(summary.yearStartPrice()).isEqualTo(154.0);
    }
}
