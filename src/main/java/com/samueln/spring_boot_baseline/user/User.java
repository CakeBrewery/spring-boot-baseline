package com.samueln.spring_boot_baseline.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Representation of a registered user.")
public record User(
    @Schema(description = "Unique identifier for the user.", example = "8b1c7aa8-6c79-4a0b-bb8c-7f8c3f0975c9")
    UUID id,

    @Schema(description = "Public username used for login and display.", example = "samdoe")
    String username,

    @Schema(description = "Email address associated with the user account.", example = "sam@example.com")
    String email,

    @Schema(description = "Given name.", example = "Sam")
    String firstName,

    @Schema(description = "Family name.", example = "Doe")
    String lastName,

    @Schema(description = "Timestamp of when the user was created.", example = "2024-01-01T12:00:00")
    LocalDateTime createdAt,

    @Schema(description = "Timestamp of the last update to the user.", example = "2024-01-10T09:30:00")
    LocalDateTime updatedAt
) {
    public static User fromEntity(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getUsername(),
            entity.getEmail(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
