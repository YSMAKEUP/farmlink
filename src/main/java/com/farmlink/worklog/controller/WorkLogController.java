package com.farmlink.worklog.controller;

import com.farmlink.worklog.domain.WorkType;
import com.farmlink.worklog.dto.WorkLogResponse;
import com.farmlink.worklog.dto.WorklogRequest;
import com.farmlink.worklog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/worklog")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;


    //등록 controller - 로그인한 사용자(userId)를 작성자로 기록
    @PostMapping("create")
    public WorkLogResponse createWorkLog(@RequestBody WorklogRequest request,
                                          @AuthenticationPrincipal Long userId) {
        return workLogService.createWorkLog(request, userId);
    }

    //기간 조회 - 로그인한 사용자와 같은 농장 데이터만
    @GetMapping("search")
    public List<WorkLogResponse> getWorkLogsByPeriod(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime started,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ended,
                                                     @AuthenticationPrincipal Long userId){
        return workLogService.getWorkLogsByPeriod(started, ended, userId);
    }

    // 카테고리 + 기간 조회 - 같은 농장 데이터만
    @GetMapping("category")
    public List<WorkLogResponse> getWorkLogsByTypeAndPeriod(
            @RequestParam WorkType workType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime started,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal Long userId) {
        return workLogService.getWorkLogsByTypeAndPeriod(workType, started, end, userId);
    }


}
