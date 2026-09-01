package com.farmlink.breedingRecord.Repository;

import com.farmlink.breedingRecord.entity.BreedingRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BreedRecordRepository extends JpaRepository<BreedingRecordEntity, Long> {

    List<BreedingRecordEntity> findByUserId_FarmCodeAndDueDateBetween(String farmCode, LocalDate start, LocalDate end);

    List<BreedingRecordEntity> findByUserId_FarmCodeAndCow_Id(String farmCode, Long cowId);

    List<BreedingRecordEntity> findByUserId_FarmCode(String farmCode);

}
