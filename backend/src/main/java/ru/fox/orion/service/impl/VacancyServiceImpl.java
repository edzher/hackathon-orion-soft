package ru.fox.orion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import ru.fox.orion.model.dto.RequestFilter;
import ru.fox.orion.model.entity.VacancyEntity;
import ru.fox.orion.repository.VacancyRepository;
import ru.fox.orion.service.VacancyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Cacheable(value = "cities", unless = "#result.isEmpty()")
    public List<String> distinctCities() {
        return mongoTemplate.findDistinct("city", VacancyEntity.class, String.class);
    }

    @Override
    @Cacheable(value = "companies", unless = "#result.isEmpty()")
    public List<String> distinctCompanies() {
        return mongoTemplate.findDistinct("company", VacancyEntity.class, String.class);
    }

    @Override
    public Page<VacancyEntity> dashboardData(RequestFilter request, Pageable pageable) {
        Query query = new Query();
        
        if (request.getCity() != null) {
            query.addCriteria(Criteria.where("city").is(request.getCity()));
        }
        if (request.getStartDate() != null) {
            query.addCriteria(Criteria.where("publishedDate").gte(request.getStartDate()));
        }
        if (request.getExperience() != null) {
            query.addCriteria(Criteria.where("experience").is(request.getExperience()));
        }
        if (request.getCompany() != null) {
            query.addCriteria(Criteria.where("company").is(request.getCompany()));
        }
        if (request.getSource() != null) {
            query.addCriteria(Criteria.where("source").is(request.getSource()));
        }
        if (request.getJobName() != null) {
            query.addCriteria(Criteria.where("jobName").regex(request.getJobName(), "i"));
        }
        
        query.with(pageable);
        
        long total = mongoTemplate.count(query, VacancyEntity.class);
        List<VacancyEntity> vacancies = mongoTemplate.find(query, VacancyEntity.class);
        
        return new PageImpl<>(vacancies, pageable, total);
    }

    @Override
    @Cacheable(value = "salaryStats", unless = "#result.isEmpty()")
    public Map<String, Object> getSalaryStatistics(String city) {
        MatchOperation matchStage = Aggregation.match(Criteria.where("city").is(city));
        
        GroupOperation groupStage = Aggregation.group()
            .avg("minSalary").as("avgMinSalary")
            .avg("maxSalary").as("avgMaxSalary")
            .min("minSalary").as("minSalary")
            .max("maxSalary").as("maxSalary");
            
        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage);
        
        return mongoTemplate.aggregate(aggregation, "vacancies", Map.class)
            .getUniqueMappedResult();
    }

    @Override
    @Cacheable(value = "trends", unless = "#result.isEmpty()")
    public List<Map<String, Object>> getJobTrends(LocalDate startDate) {
        MatchOperation matchStage = Aggregation.match(
            Criteria.where("publishedDate").gte(startDate)
        );
        
        GroupOperation groupStage = Aggregation.group("jobName")
            .count().as("count")
            .avg("minSalary").as("avgSalary");
            
        SortOperation sortStage = Aggregation.sort(Sort.Direction.DESC, "count");
        LimitOperation limitStage = Aggregation.limit(10);
        
        Aggregation aggregation = Aggregation.newAggregation(
            matchStage, groupStage, sortStage, limitStage
        );
        
        return mongoTemplate.aggregate(aggregation, "vacancies", Map.class)
            .getMappedResults();
    }

    @Override
    @Cacheable(value = "companyStats", unless = "#result.isEmpty()")
    public List<Map<String, Object>> getTopCompanies() {
        GroupOperation groupStage = Aggregation.group("company")
            .count().as("vacancyCount")
            .avg("minSalary").as("avgSalary");
            
        SortOperation sortStage = Aggregation.sort(Sort.Direction.DESC, "vacancyCount");
        LimitOperation limitStage = Aggregation.limit(10);
        
        Aggregation aggregation = Aggregation.newAggregation(
            groupStage, sortStage, limitStage
        );
        
        return mongoTemplate.aggregate(aggregation, "vacancies", Map.class)
            .getMappedResults();
    }

    @Override
    public List<Map<String, Object>> getSkillsDemand() {
        GroupOperation groupStage = Aggregation.group("skills")
            .count().as("demand")
            .avg("minSalary").as("avgSalary");
            
        SortOperation sortStage = Aggregation.sort(Sort.Direction.DESC, "demand");
        
        Aggregation aggregation = Aggregation.newAggregation(
            groupStage, sortStage
        );
        
        return mongoTemplate.aggregate(aggregation, "vacancies", Map.class)
            .getMappedResults();
    }
}
