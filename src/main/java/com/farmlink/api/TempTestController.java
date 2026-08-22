package com.farmlink.api;

import com.farmlink.security.JwtTokenProvider;
import com.farmlink.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TempTestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/test/jwt")
    public String testJwt() {
        Long userId = 0L;
        String email = "test@farmlink.com";

        String accessToken = jwtTokenProvider.createAccessToken(userId, email);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, email);

        refreshTokenService.save(email, refreshToken, 604800000L);

        String savedToken = refreshTokenService.get(email);

        return "Access: " + accessToken
                + "\nRefresh: " + refreshToken
                + "\nRedis 저장 확인: " + savedToken.equals(refreshToken);
    }
}