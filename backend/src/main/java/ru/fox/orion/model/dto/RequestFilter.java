package ru.fox.orion.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Фильтр для выборки вакансий")
public class RequestFilter {

    @Schema(description = "Город")
    private String city;

    @Schema(description = "Дата публикации вакансии")
    private LocalDate startDate;

    @Schema(description = "Опыт работы")
    private String experience;

    @Schema(description = "Компания")
    private String company;

    @Schema(description = "Источник вакансии")
    private String source;

    @Schema(description = "Бенефиты")
    private String benefits;

    @Schema(description = "Позиция")
    private String jobName;

}
