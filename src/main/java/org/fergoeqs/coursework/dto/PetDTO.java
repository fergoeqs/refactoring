package org.fergoeqs.coursework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.fergoeqs.coursework.models.enums.PetType;
import org.fergoeqs.coursework.models.enums.SexEnum;

import java.math.BigDecimal;

@Schema(description = "DTO питомца")
public record PetDTO(
        @Schema(description = "ID питомца", example = "1")
        Long id,
        @Schema(description = "Имя питомца", example = "Барсик")
        @NotBlank(message = "Имя питомца не может быть пустым")
        String name,
        @Schema(description = "Порода", example = "Персидская")
        String breed,
        @Schema(description = "Тип питомца", example = "CAT")
        @NotNull(message = "Тип питомца обязателен")
        PetType type,
        @Schema(description = "Вес в кг", example = "4.5")
        BigDecimal weight,
        @Schema(description = "Пол", example = "MALE")
        SexEnum sex,
        @Schema(description = "Возраст в годах", example = "3")
        Integer age,
        @Schema(description = "ID ветеринара", example = "1")
        Long actualVet,
        @Schema(description = "ID владельца", example = "1")
        Long owner,
        @Schema(description = "ID сектора", example = "1")
        Long sector,
        @Schema(description = "URL фотографии")
        String photoUrl) {
}
