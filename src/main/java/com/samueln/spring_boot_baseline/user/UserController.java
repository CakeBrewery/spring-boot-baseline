package com.samueln.spring_boot_baseline.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User management APIs")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}/favorites")
    @Tag(name = "Favorites", description = "Stock favorite management APIs")
    @Operation(summary = "Get user favorites", description = "Returns a list of favorited stocks for a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favorites returned successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FavoriteStock.class)))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public List<FavoriteStock> getUserFavorites(
            @Parameter(description = "The UUID of the user.", required = true) @PathVariable("userId") UUID userId) {
        return userService.getUserFavorites(userId);
    }

    @PostMapping("/{userId}/favorites")
    @Tag(name = "Favorites")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a favorite", description = "Adds a stock to the user's favorites list.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Favorite added successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input or stock already favorited", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public void addFavorite(
            @Parameter(description = "The UUID of the user.", required = true) @PathVariable("userId") UUID userId,
            @RequestBody AddFavoriteRequest request) {
        userService.addFavorite(userId, request.getSymbol());
    }

    @DeleteMapping("/{userId}/favorites/{symbol}")
    @Tag(name = "Favorites")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a favorite", description = "Removes a stock from the user's favorites list.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorite removed successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "User or favorite not found", content = @Content)
    })
    public void removeFavorite(
            @Parameter(description = "The UUID of the user.", required = true) @PathVariable("userId") UUID userId,
            @Parameter(description = "The stock symbol to remove.", required = true) @PathVariable("symbol") String symbol) {
        userService.removeFavorite(userId, symbol);
    }
}
