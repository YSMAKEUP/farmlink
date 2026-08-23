package com.farmlink.worklog.service;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import com.farmlink.worklog.domain.WorkLogEntity;
import com.farmlink.worklog.domain.WorkType;
import com.farmlink.worklog.dto.WorkLogResponse;
import com.farmlink.worklog.dto.WorklogRequest;
import com.farmlink.worklog.repository.WorkLogRepository;
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
    private final UserRepository userRepository;

    // 소의 존재 여부를 확인
    private CowEntity resolveCow(Long cowId) {
        if (cowId == null) {
            return null;
        }
        return cowRepository.findById(cowId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소입니다. id=" + cowId));
    }

    // 인증된 사용자 조회 (없으면 토큰은 유효한데 DB에 유저가 없는 이상 상태 -> 400)
    private UserEntity resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
    }

    // 등록 - 로그인한 사용자를 작성자로 기록
    @Transactional
    public WorkLogResponse createWorkLog(WorklogRequest request, Long userId) {
        CowEntity cow = resolveCow(request.getCowId());
        UserEntity user = resolveUser(userId);

        WorkLogEntity workLog = WorkLogEntity.builder()
                .workDateTime(request.getWorkDateTime())
                .workType(request.getWorkType())
                .cow(cow)
                .content(request.getContent())
                .userId(user)
                .build();

        WorkLogEntity saved = workLogRepository.save(workLog);
        return new WorkLogResponse(saved);
    }

    // 기간 조회 - 로그인한 사용자와 같은 농장(farmCode) 데이터만
    public List<WorkLogResponse> getWorkLogsByPeriod(LocalDateTime start, LocalDateTime end, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        return workLogRepository.findByUserId_FarmCodeAndWorkDateTimeBetween(farmCode, start, end)
                .stream()
                .map(WorkLogResponse::new)
                .toList();
    }

    // 카테고리 + 기간 조회 - 같은 농장 데이터만
    public List<WorkLogResponse> getWorkLogsByTypeAndPeriod(WorkType workType, LocalDateTime start, LocalDateTime end, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        return workLogRepository.findByUserId_FarmCodeAndWorkTypeAndWorkDateTimeBetween(farmCode, workType, start, end)
                .stream()
                .map(WorkLogResponse::new)
                .toList();
    }
}