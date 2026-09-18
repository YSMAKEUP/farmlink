package com.farmlink.milk.repository;
import com.farmlink.milk.domain.MilkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MilkRepository extends JpaRepository<MilkRecord,Long> {

    // 특정 소 목록 조회 - 같은 농장(cow.registeredBy.farmCode) 데이터만
   List<MilkRecord> findByCow_RegisteredBy_FarmCodeAndCow_Id(String farmCode, Long cowId);

    // 날짜 범위 목록 조회 - 같은 농장 데이터만
    List<MilkRecord> findByCow_RegisteredBy_FarmCodeAndMilkedDateBetween(String farmCode, LocalDate startDate, LocalDate endDate);

    // 이상감지용 - 특정 소의 기간별 착유기록 조회 (같은 농장 데이터만)
    List<MilkRecord> findByCow_RegisteredBy_FarmCodeAndCow_IdAndMilkedDateBetween(
            String farmCode, Long cowId, LocalDate startDate, LocalDate endDate);
}
