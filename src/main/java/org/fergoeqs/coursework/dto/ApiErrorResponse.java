package org.fergoeqs.coursework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Стандартный формат ответа об ошибке")
public record ApiErrorResponse(
        @Schema(description = "HTTP статус код", example = "400")
        int status,
        @Schema(description = "Сообщение об ошибке", example = "Validation failed")
        String message,
        @Schema(description = "Время возникновения ошибки")
        LocalDateTime timestamp,
        @Schema(description = "Путь запроса", example = "/api/pets")
        String path
) {
    public ApiErrorResponse(int status, String message, String path) {
        this(status, message, LocalDateTime.now(), path);
    }
}
