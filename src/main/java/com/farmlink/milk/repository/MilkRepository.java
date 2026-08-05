package com.farmlink.milk.repository;
import com.farmlink.milk.domain.MilkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MilkRepository extends JpaRepository<MilkRecord,Long> {

    //특정 소 목록 조회
   List<MilkRecord> findByCow_Id(Long cowId);

    // 날짜 범위 목록 조회
    List<MilkRecord> findByMilkedDateBetween(LocalDate startDate, LocalDate endDate);
}
