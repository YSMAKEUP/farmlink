package com.farmlink.breedingRecord.Repository;

import com.farmlink.breedingRecord.entity.BreedingRecordEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BreedRecordRepository extends JpaRepository<BreedingRecordEntity, Long> {

    @EntityGraph(attributePaths = {"cow", "userId"})
    List<BreedingRecordEntity> findByUserId_FarmCodeAndDueDateBetween(String farmCode, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = {"cow", "userId"})
    List<BreedingRecordEntity> findByUserId_FarmCodeAndCow_Id(String farmCode, Long cowId);

    @EntityGraph(attributePaths = {"cow", "userId"})
    List<BreedingRecordEntity> findByUserId_FarmCode(String farmCode);

}
