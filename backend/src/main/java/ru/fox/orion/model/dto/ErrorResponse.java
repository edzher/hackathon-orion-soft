package ru.fox.orion.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Базовая модель, описывает ответы на запросы с ошибкой (неуспешные)")
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse{

    @Schema(description = "Система где произошла ошибка", nullable = true)
    private String source;

    @Schema(description = "Ошибка")
    private ApiError error;

    public ErrorResponse(ApiError error) {
        this.error = error;
    }

}

