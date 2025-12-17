package com.samueln.spring_boot_baseline.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteStockRepository extends JpaRepository<FavoriteStockEntity, UUID> {
    List<FavoriteStockEntity> findByUserId(UUID userId);
    Optional<FavoriteStockEntity> findByUserIdAndSymbol(UUID userId, String symbol);
    void deleteByUserIdAndSymbol(UUID userId, String symbol);
}
