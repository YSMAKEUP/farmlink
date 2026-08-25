package com.farmlink.auth.service;

import com.farmlink.auth.dto.RefreshResponseDto;
import com.farmlink.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String,String> redisTemplate;

    public RefreshResponseDto refresh(String refreshToken){
        if  (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 refresh token입니다.");
        }
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String email = jwtTokenProvider.getEmail(refreshToken);

        String savedToken = redisTemplate.opsForValue().get("refreshToken:" + userId);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("일치하지 않거나 만료된 refresh token입니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId,email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId,email);


        redisTemplate.opsForValue().set(
           "refreshToken:" + userId,
                newRefreshToken,
                Duration.ofDays(7)
        );

        return RefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
