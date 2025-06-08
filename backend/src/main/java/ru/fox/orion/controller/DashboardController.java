package ru.fox.orion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.fox.orion.core.constants.ApiKeys;
import ru.fox.orion.model.dto.RequestFilter;
import ru.fox.orion.model.entity.VacancyEntity;
import ru.fox.orion.service.VacancyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiKeys.V1 + "/dashboard")
@Tag(name = "Dashboard API")
public class DashboardController {

    private final VacancyService vacancyService;

    @Operation(
            summary = "Список городов вакансий"
    )
    @GetMapping("/city")
    public ResponseEntity<List<String>> cities() {
        return ResponseEntity.ok(vacancyService.distinctCities());
    }

    @Operation(
            summary = "Список компаний"
    )
    @GetMapping("/company")
    public ResponseEntity<List<String>> companies() {
        return ResponseEntity.ok(vacancyService.distinctCompanies());
    }

    @Operation(
            summary = "Данные о вакансиях с пагинацией"
    )
    @PostMapping
    public ResponseEntity<Page<VacancyEntity>> data(
            @RequestBody RequestFilter request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(vacancyService.dashboardData(request, pageable));
    }

    @Operation(
            summary = "Статистика зарплат по городу"
    )
    @GetMapping("/salary-stats")
    public ResponseEntity<Map<String, Object>> salaryStats(@RequestBody String city) {
        return ResponseEntity.ok(vacancyService.getSalaryStatistics(city));
    }

    @Operation(
            summary = "Тренды вакансий"
    )
    /*@GetMapping("/trends")
    public ResponseEntity<List<Map<String, Object>>> trends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(vacancyService.getJobTrends(startDate));
    }

    @Operation(
            summary = "Топ компаний"
    )
    @GetMapping("/top-companies")
    public ResponseEntity<List<Map<String, Object>>> topCompanies() {
        return ResponseEntity.ok(vacancyService.getTopCompanies());
    }

    @Operation(
            summary = "Спрос на навыки"
    )
    @GetMapping("/skills-demand")
    public ResponseEntity<List<Map<String, Object>>> skillsDemand() {
        return ResponseEntity.ok(vacancyService.getSkillsDemand());
    }*/

    @PostMapping("/report")
    public void report(@RequestBody RequestFilter request) {
        return;
    }

}
