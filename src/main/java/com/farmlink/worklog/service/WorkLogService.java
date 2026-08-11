package com.farmlink.worklog.service;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.worklog.domain.WorkLogEntity;
import com.farmlink.worklog.domain.WorkType;
import com.farmlink.worklog.dto.WorkLogResponse;
import com.farmlink.worklog.dto.WorklogRequest;
import com.farmlink.worklog.repository.WorkLogRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final CowRepository cowRepository;

    // 소의 존재 여부를 확인
    private CowEntity resolveCow(Long cowId) {
        if (cowId == null) {
            return null;
        }
        return cowRepository.findById(cowId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소입니다. id=" + cowId));
    }

    // 등록
    @Transactional
    public WorkLogResponse createWorkLog(WorklogRequest request) {
        CowEntity cow = resolveCow(request.getCowId());

        WorkLogEntity workLog = WorkLogEntity.builder()
                .workDateTime(request.getWorkDateTime())
                .workType(request.getWorkType())
                .cow(cow)
                .content(request.getContent())
                .build();

        WorkLogEntity saved = workLogRepository.save(workLog);
        return new WorkLogResponse(saved);
    }

    // 기간 조회
    public List<WorkLogResponse> getWorkLogsByPeriod(LocalDateTime start, LocalDateTime end) {
        return workLogRepository.findByWorkDateTimeBetween(start, end)
                .stream()
                .map(WorkLogResponse::new)
                .toList();
    }

    // 카테고리 + 기간 조회
    public List<WorkLogResponse> getWorkLogsByTypeAndPeriod(WorkType workType, LocalDateTime start, LocalDateTime end) {
        return workLogRepository.findByWorkTypeAndWorkDateTimeBetween(workType, start, end)
                .stream()
                .map(WorkLogResponse::new)
                .toList();
    }
}