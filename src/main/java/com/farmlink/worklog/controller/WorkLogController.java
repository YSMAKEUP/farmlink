package com.farmlink.worklog.controller;

import com.farmlink.worklog.domain.WorkType;
import com.farmlink.worklog.dto.WorkLogResponse;
import com.farmlink.worklog.dto.WorklogRequest;
import com.farmlink.worklog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/worklog")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;


    //등록 controller
    @PostMapping("create")
    public WorkLogResponse createWorkLog(@RequestBody WorklogRequest request){
        return  workLogService.createWorkLog(request);
    }

    //기간 조회
    @GetMapping("search")
    public List<WorkLogResponse> getWorkLogsByPeriod(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime started,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ended){
        return workLogService.getWorkLogsByPeriod(started, ended);
    }

    // 카테고리 + 기간 조회
    @GetMapping("category")
    public List<WorkLogResponse> getWorkLogsByTypeAndPeriod(
            @RequestParam WorkType workType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime started,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return workLogService.getWorkLogsByTypeAndPeriod(workType, started, end);
    }


}
