package ru.fox.orion.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import com.itextpdf.text.Font;
import ru.fox.orion.model.dto.SalaryByDayDto;
import ru.fox.orion.dto.RequestFilter;
import ru.fox.orion.util.FilterHashUtil;
import ru.fox.orion.service.ReportCacheService;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MongoTemplate mongoTemplate;
    private final ReportCacheService reportCacheService;
    private static final com.itextpdf.text.Font TITLE_FONT = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

    public byte[] generateVacancyReport(LocalDate startDate, LocalDate endDate) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Заголовок
            addTitle(document, "Отчет по вакансиям");
            addSubtitle(document, "Период: " + startDate.format(DateTimeFormatter.ISO_DATE) + 
                    " - " + endDate.format(DateTimeFormatter.ISO_DATE));

            // Статистика по городам
            addCityStatistics(document);

            // График распределения зарплат
            addSalaryDistributionChart(document);

            // Топ компаний
            addTopCompanies(document);

            // Статистика по опыту работы
            addExperienceStatistics(document);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new DocumentException("Error generating PDF report: " + e.getMessage(), e);
        }
    }

    private void addTitle(Document document, String title) throws DocumentException {
        Paragraph p = new com.itextpdf.text.Paragraph(title, TITLE_FONT);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(20);
        document.add(p);
    }

    private void addSubtitle(Document document, String subtitle) throws DocumentException {
        Paragraph p = new com.itextpdf.text.Paragraph(subtitle, SUBTITLE_FONT);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(20);
        document.add(p);
    }

    private void addCityStatistics(Document document) throws DocumentException {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.group("город")
                .count().as("count")
                .avg("зарплата_от").as("avgSalary")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
            aggregation, "vacancies", Map.class);

        document.add(new Paragraph("Статистика по городам:", SUBTITLE_FONT));
        
        for (Map result : results.getMappedResults()) {
            String city = (String) result.get("_id");
            Integer count = (Integer) result.get("count");
            Double avgSalary = (Double) result.get("avgSalary");
            
            Paragraph p = new com.itextpdf.text.Paragraph(
                String.format("%s: %d вакансий, средняя зарплата %.2f", 
                    city, count, avgSalary),
                NORMAL_FONT
            );
            document.add(p);
        }
        
        document.add(new Paragraph("\n"));
    }

    private void addSalaryDistributionChart(Document document) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Получаем данные о зарплатах
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.group()
                .avg("зарплата_от").as("avgMinSalary")
                .avg("зарплата_до").as("avgMaxSalary")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
            aggregation, "vacancies", Map.class);
        
        Map result = results.getUniqueMappedResult();
        if (result != null) {
            dataset.addValue((Double) result.get("avgMinSalary"), "Минимальная", "Зарплата");
            dataset.addValue((Double) result.get("avgMaxSalary"), "Максимальная", "Зарплата");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Распределение зарплат",
            "Тип зарплаты",
            "Сумма",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            false,
            false
        );
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.getRenderer().setSeriesPaint(0, new Color(0x4F81BD));
        plot.getRenderer().setSeriesPaint(1, new Color(0xC0504D));
        plot.getRenderer().setDefaultItemLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        plot.getRenderer().setDefaultItemLabelsVisible(true);

        ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOut, chart, 500, 300);
        
        Image chartImage = Image.getInstance(chartOut.toByteArray());
        document.add(chartImage);
        document.add(new Paragraph("\n"));
    }

    private void addTopCompanies(Document document) throws DocumentException {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.group("компания")
                .count().as("count")
                .avg("зарплата_от").as("avgSalary"),
            Aggregation.sort(Sort.Direction.DESC, "count"),
            Aggregation.limit(10)
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
            aggregation, "vacancies", Map.class);

        document.add(new Paragraph("Топ-10 компаний по количеству вакансий:", SUBTITLE_FONT));
        
        for (Map result : results.getMappedResults()) {
            String company = (String) result.get("_id");
            Integer count = (Integer) result.get("count");
            Double avgSalary = (Double) result.get("avgSalary");
            
            Paragraph p = new com.itextpdf.text.Paragraph(
                String.format("%s: %d вакансий, средняя зарплата %.2f", 
                    company, count, avgSalary),
                NORMAL_FONT
            );
            document.add(p);
        }
        
        document.add(new Paragraph("\n"));
    }

    private void addExperienceStatistics(Document document) throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.group("стаж")
                .count().as("count")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
            aggregation, "vacancies", Map.class);
        
        for (Map result : results.getMappedResults()) {
            Integer experience = (Integer) result.get("_id");
            Integer count = (Integer) result.get("count");
            dataset.setValue(experience + " лет", count);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Распределение по опыту работы",
            dataset,
            false,
            false,
            false
        );

        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setSectionOutlinesVisible(false);
        Color[] colors = {
            new Color(0x4F81BD),
            new Color(0xC0504D),
            new Color(0x9BBB59),
            new Color(0x8064A2),
            new Color(0xF79646),
            new Color(0x2C4D75)
        };
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, colors[i % colors.length]);
            i++;
        }
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0}: {1}"));

        ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOut, chart, 500, 300);
        
        Image chartImage = Image.getInstance(chartOut.toByteArray());
        document.add(chartImage);
    }

    public List<SalaryByDayDto> getAvgSalaryByDay(String job, LocalDate from, LocalDate to) {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("должность").is(job)
                        .andOperator(
                            Criteria.where("дата_размещения").gte(from),
                            Criteria.where("дата_размещения").lte(to)
                        )
            ),
            Aggregation.project()
                .and(DateOperators.DateToString.dateOf("дата_размещения").toString("%Y-%m-%d")).as("date")
                .andExpression("({ $add: [ '$зарплата_от', '$зарплата_до' ] } / 2)").as("salary"),
            Aggregation.group("date")
                .avg("salary").as("avgSalary"),
            Aggregation.sort(Sort.Direction.ASC, "_id")
        );

        AggregationResults<SalaryByDayDto> results = mongoTemplate.aggregate(
            aggregation, "vacancies", SalaryByDayDto.class
        );
        return results.getMappedResults();
    }

    public byte[] getOrGenerateReport(RequestFilter filter) throws DocumentException {
        String hash = FilterHashUtil.getFilterHash(filter);
        String s3Key = reportCacheService.getS3KeyByHash(hash);
        if (s3Key != null) {
            return reportCacheService.getPdfFromS3(s3Key);
        }
        byte[] pdf = generateVacancyReport(filter.getStartDate(), filter.getEndDate());
        reportCacheService.savePdfToS3(hash, pdf);
        reportCacheService.saveToCache(hash, hash);
        return pdf;
    }
}