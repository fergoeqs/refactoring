package org.fergoeqs.coursework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO для аутентификации пользователя")
public record LoginUserDTO(
        @Schema(description = "Имя пользователя", example = "john_doe", required = true)
        @NotBlank(message = "Имя пользователя не может быть пустым")
        String username,
        @Schema(description = "Пароль", example = "password123", required = true)
        @NotBlank(message = "Пароль не может быть пустым")
        String password
) {
}