package com.farmlink.worklog.repository;

import com.farmlink.worklog.domain.WorkLogEntity;
import com.farmlink.worklog.domain.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;


public interface WorkLogRepository extends JpaRepository<WorkLogEntity,Long> {

    //기간 조회 (같은 농장 소속 사용자들 것만 - userId(작성자) -> farmCode 기준)
    List<WorkLogEntity> findByUserId_FarmCodeAndWorkDateTimeBetween(String farmCode, LocalDateTime start, LocalDateTime end);

    //enum별로 조회 (같은 농장 소속만)
    List<WorkLogEntity> findByUserId_FarmCodeAndWorkTypeAndWorkDateTimeBetween(String farmCode, WorkType workType, LocalDateTime start, LocalDateTime end);




}
