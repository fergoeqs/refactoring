package org.fergoeqs.coursework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO для регистрации нового пользователя")
public record RegisterUserDTO(
        @Schema(description = "Имя пользователя", example = "john_doe", required = true)
        @NotBlank(message = "Имя пользователя не может быть пустым")
        String username,
        @Schema(description = "Email адрес", example = "john@example.com", required = true)
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Email должен быть в правильном формате")
        String email,
        @Schema(description = "Пароль", example = "password123", required = true)
        @NotBlank(message = "Пароль не может быть пустым")
        String password,
        @Schema(description = "Номер телефона", example = "+7 (999) 123-45-67", required = true)
        @NotBlank(message = "Номер телефона не может быть пустым")
        String phoneNumber,
        @Schema(description = "Имя", example = "John")
        @NotBlank(message = "Имя не может быть пустым")
        String name,
        @Schema(description = "Фамилия", example = "Doe")
        @NotBlank(message = "Фамилия не может быть пустой")
        String surname
) {}
