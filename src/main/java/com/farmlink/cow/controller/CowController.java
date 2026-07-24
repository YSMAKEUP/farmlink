package com.farmlink.cow.controller;


import com.farmlink.cow.Service.CowService;
import com.farmlink.cow.dto.CowRequest;
import com.farmlink.cow.dto.CowResponse;
import lombok.RequiredArgsConstructor;
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
}
