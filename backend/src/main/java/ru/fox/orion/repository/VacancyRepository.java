package ru.fox.orion.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.fox.orion.model.entity.VacancyEntity;

import java.util.UUID;

@Repository
public interface VacancyRepository extends MongoRepository<VacancyEntity, UUID> {



}
