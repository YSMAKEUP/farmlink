package com.farmlink.worklog.repository;

import com.farmlink.worklog.domain.WorkLogEntity;
import com.farmlink.worklog.domain.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;


public interface WorkLogRepository extends JpaRepository<WorkLogEntity,Long> {

    //기간 조회
    List<WorkLogEntity>findByWorkDateTimeBetween(LocalDateTime start, LocalDateTime end);

    //enum별로 조회
    List<WorkLogEntity>findByWorkTypeAndWorkDateTimeBetween(WorkType workType, LocalDateTime start, LocalDateTime end);




}
