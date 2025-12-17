package com.samueln.spring_boot_baseline.stock;

import com.samueln.spring_boot_baseline.stock.dto.StockSummary;
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

    @Operation(summary = "Get a pre-computed summary for a symbol")
    @GetMapping("/summary")
    public StockSummary getStockSummary(@RequestParam("symbol") String symbol) {
        return stockService.getStockSummary(symbol);
    }
}
