package ru.fox.orion.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Базовая модель, описывает ошибку")
@Getter
@Setter
public class ApiError {

    @Schema(description = "HTTP код ошибки")
    private Integer code;

    @Schema(description = "Сообщение ошибки")
    private String message;

    public ApiError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
