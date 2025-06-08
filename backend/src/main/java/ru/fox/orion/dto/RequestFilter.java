package ru.fox.orion.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RequestFilter {

    private String job;

    private LocalDate startDate;

    private LocalDate endDate;

} 