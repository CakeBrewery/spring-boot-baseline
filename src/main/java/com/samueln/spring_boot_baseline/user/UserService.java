package com.samueln.spring_boot_baseline.user;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final FavoriteStockRepository favoriteStockRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(User::fromEntity)
                .toList();
    }

    public List<FavoriteStock> getUserFavorites(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return favoriteStockRepository.findByUserId(userId)
                .stream()
                .map(FavoriteStock::fromEntity)
                .toList();
    }

    @Transactional
    public void addFavorite(UUID userId, String symbol) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (favoriteStockRepository.findByUserIdAndSymbol(userId, symbol).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock already favorited");
        }

        FavoriteStockEntity favorite = FavoriteStockEntity.builder()
                .user(user)
                .symbol(symbol)
                .build();

        favoriteStockRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(UUID userId, String symbol) {
        FavoriteStockEntity favorite = favoriteStockRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User or favorite not found"));

        favoriteStockRepository.delete(favorite);
    }
}