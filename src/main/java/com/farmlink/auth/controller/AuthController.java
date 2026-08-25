package com.farmlink.auth.controller;

import com.farmlink.auth.dto.RefreshRequestDto;
import com.farmlink.auth.dto.RefreshResponseDto;
import com.farmlink.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDto> refresh(@RequestBody RefreshRequestDto request) {
        RefreshResponseDto response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}