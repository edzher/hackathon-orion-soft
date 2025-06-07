package ru.fox.orion.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vacancies")
public class VacancyEntity {

    @Id
    private String id;

    @Indexed
    private String source;

    private String minSalary;

    private String maxSalary;

    @Indexed
    private String url;

    @Indexed
    private String city;

    @Indexed
    private String experience;
    
    @Indexed
    private String company;
    
    @Indexed
    private String jobName;
    
    @Indexed
    private LocalDate publishedDate;
}
