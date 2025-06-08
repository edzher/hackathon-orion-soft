package ru.fox.orion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.fox.orion.model.dto.RequestFilter;
import ru.fox.orion.model.dto.SalaryStatistic;
import ru.fox.orion.model.entity.VacancyEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface VacancyService {

    List<String> distinctCities();

    List<String> distinctCompanies();

    Page<VacancyEntity> dashboardData(RequestFilter request, Pageable pageable);
    
    Map<String, Object> getSalaryStatistics(String city);

    SalaryStatistic getSalaryStatistic(RequestFilter filter);
    
    //List<Map<String, Object>> getJobTrends(LocalDate startDate);
    
    /*List<Map<String, Object>> getTopCompanies();
    
    List<Map<String, Object>> getSkillsDemand();*/
}
