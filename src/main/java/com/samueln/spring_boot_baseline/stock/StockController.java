package com.samueln.spring_boot_baseline.stock;

import com.samueln.spring_boot_baseline.stock.dto.CompanyOverview;
import com.samueln.spring_boot_baseline.stock.dto.GlobalQuote;
import com.samueln.spring_boot_baseline.stock.dto.TimeSeriesMonthly;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @Operation(summary = "Get Global Quote for a symbol")
    @GetMapping("/global-quote")
    public GlobalQuote getGlobalQuote(@RequestParam("symbol") String symbol) {
        return stockService.getGlobalQuote(symbol);
    }

    @Operation(summary = "Get Company Overview for a symbol")
    @GetMapping("/company-overview")
    public CompanyOverview getCompanyOverview(@RequestParam("symbol") String symbol) {
        return stockService.getCompanyOverview(symbol);
    }

    @Operation(summary = "Get Monthly Time Series for a symbol")
    @GetMapping("/monthly-time-series")
    public TimeSeriesMonthly getMonthlyTimeSeries(@RequestParam("symbol") String symbol) {
        return stockService.getMonthlyTimeSeries(symbol);
    }
}
