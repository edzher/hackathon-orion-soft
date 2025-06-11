package ru.fox.orion.controller;

import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.fox.orion.model.dto.SalaryByDayDto;
import ru.fox.orion.service.ReportService;
import ru.fox.orion.dto.RequestFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/vacancies")
    public ResponseEntity<byte[]> generateVacancyReport(@RequestBody RequestFilter filter) {
        try {
            byte[] report = reportService.getOrGenerateReport(filter);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "vacancy_report.pdf");
            return ResponseEntity.ok().headers(headers).body(report);
        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/salary-by-day")
    public List<SalaryByDayDto> getSalaryByDay(
            //@RequestParam String job,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.getAvgSalaryByDay("Курьер", from, to);
    }

} 