package ru.fox.orion.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vacancies")
@CompoundIndex(name = "idx_city_job", def = "{'city': 1, 'position': 1}")
@CompoundIndex(name = "idx_company_date", def = "{'company': 1, 'publishedDate': -1}")
@CompoundIndex(name = "idx_salary", def = "{'minSalary': 1, 'maxSalary': 1}")
public class VacancyEntity {

    @Id
    private String id;

    @Field("minSalary")
    @Indexed
    private Integer minSalary;

    @Field("maxSalary")
    @Indexed
    private Integer maxSalary;

    @Field("source")
    @Indexed
    private String source;

    @Field("position")
    @Indexed
    private String position;

    @Field("publishedDate")
    @Indexed
    private LocalDate publishedDate;

    @Field("experience")
    @Indexed
    private Integer experience;

    @Field("city")
    @Indexed
    private String city;

    @Field("company")
    @Indexed
    private String company;

    @Field("benefits")
    private List<String> benefits;

    @Field("employmentType")
    @Indexed
    private String employmentType;

}
