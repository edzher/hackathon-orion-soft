package ru.fox.orion.model.dto;

import lombok.Data;

@Data
public class SalaryStatistic {
    private double median;
    private double maximum;
    private double minimum;
    private String start_date;
    private String end_date;
}