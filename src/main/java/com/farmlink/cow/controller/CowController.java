package com.farmlink.cow.controller;

import com.farmlink.cow.Service.CowService;
import com.farmlink.cow.domain.CowStatus;
import com.farmlink.cow.dto.CowRequest;
import com.farmlink.cow.dto.CowResponse;
import com.farmlink.cow.dto.CowSearchCondition;
import com.farmlink.cow.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cows")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5174")
public class CowController {

    private final CowService cowService;

    @PostMapping
    public ResponseEntity<CowResponse> registerCow(@RequestBody CowRequest request) {
        CowResponse response =  cowService.registerCow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<CowResponse>> findCows(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CowStatus status,
            Pageable pageable
    ) {
        CowSearchCondition condition = new CowSearchCondition(keyword, status);
        PageResponse<CowResponse> response = cowService.findCows(condition, pageable);
        return ResponseEntity.ok(response);
    }
}