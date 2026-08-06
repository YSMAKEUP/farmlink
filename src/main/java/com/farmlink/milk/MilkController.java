package com.farmlink.milk;

import com.farmlink.milk.dto.MilkRecordRequest;
import com.farmlink.milk.dto.MilkRecordResponse;
import com.farmlink.milk.service.MilkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/milk-records")
@RequiredArgsConstructor
public class MilkController {

    private final MilkService milkService;

    //착유 기록 등록
    @PostMapping
    public ResponseEntity<MilkRecordResponse> register(@RequestBody MilkRecordRequest request){
        MilkRecordResponse response = milkService.registerRecord(request);
        return ResponseEntity.ok(response);
    }

    //착유 기록 소별로 기록 조회
    @GetMapping("/cow/{cowId}")
    public ResponseEntity<List<MilkRecordResponse>> findByCow(@PathVariable Long cowId){
        List<MilkRecordResponse> response = milkService.getMilkRecordsByCow(cowId);
        return ResponseEntity.ok(response);
    }

    //착유 기록 기간별 조회
    @GetMapping
    public ResponseEntity<List<MilkRecordResponse>> findByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ){
        List<MilkRecordResponse> responses = milkService.getMilkRecordsByDateRange(start, end);
        return ResponseEntity.ok(responses);
    }
}