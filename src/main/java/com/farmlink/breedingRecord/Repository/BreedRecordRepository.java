package com.farmlink.breedingRecord.Repository;

import com.farmlink.breedingRecord.entity.BreedingRecordEntity;
import com.farmlink.breedingRecord.entity.PregnancyResult;
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

    // AI 질의응답 기능(get_pending_breeding_count)에서 사용 - 목록을 다 불러와서 세는 대신 집계 쿼리로 처리
    long countByUserId_FarmCodeAndCheckResult(String farmCode, PregnancyResult checkResult);
}
