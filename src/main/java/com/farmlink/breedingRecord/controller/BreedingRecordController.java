package com.farmlink.breedingRecord.controller;

import com.farmlink.breedingRecord.Service.BreedingRecordService;
import com.farmlink.breedingRecord.dto.BreedingRequestDto;
import com.farmlink.breedingRecord.dto.BreedingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breeding-records")
@RequiredArgsConstructor
public class BreedingRecordController {

    private final BreedingRecordService breedingRecordService;

    // 등록 - 로그인한 사용자를 작성자로 기록
    @PostMapping("create")
    public BreedingResponseDto create(@RequestBody BreedingRequestDto request,
                                       @AuthenticationPrincipal Long userId) {
        return breedingRecordService.create(request, userId);
    }

    // 월별 조회 - 캘린더 점 표시용, 로그인한 사용자와 같은 농장 데이터만
    @GetMapping("month")
    public List<BreedingResponseDto> getByMonth(@RequestParam int year,
                                                 @RequestParam int month,
                                                 @AuthenticationPrincipal Long userId) {
        return breedingRecordService.getByMonth(year, month, userId);
    }

    // 개체별 조회 - 같은 농장 데이터만
    @GetMapping("cow/{cowId}")
    public List<BreedingResponseDto> getByCow(@PathVariable Long cowId,
                                               @AuthenticationPrincipal Long userId) {
        return breedingRecordService.getByCow(cowId, userId);
    }
}
