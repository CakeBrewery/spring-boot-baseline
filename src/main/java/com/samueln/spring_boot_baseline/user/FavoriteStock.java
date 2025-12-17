package com.samueln.spring_boot_baseline.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStock {
    private String symbol;
    private LocalDateTime addedAt;

    public static FavoriteStock fromEntity(FavoriteStockEntity entity) {
        return FavoriteStock.builder()
                .symbol(entity.getSymbol())
                .addedAt(entity.getAddedAt())
                .build();
    }
}
