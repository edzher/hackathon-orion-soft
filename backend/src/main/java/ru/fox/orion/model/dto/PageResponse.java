package ru.fox.orion.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Базовая модель, описывает постраничное представление")
public record PageResponse<T>(

        @Schema(description = "Элементы постраничного представления")
        List<T> items,

        @Schema(description = "Номер страницы")
        int page,

        @Schema(description = "Общее число страниц")
        int totalPages,

        @Schema(description = "Общее число элементов")
        long totalElements,

        @Schema(description = "Размер страницы")
        int size
) {

    public PageResponse(Page<T> pageable) {
        this(pageable.getContent(),
                pageable.getNumber(),
                pageable.getTotalPages(),
                pageable.getTotalElements(),
                pageable.getSize()
        );
    }

}
