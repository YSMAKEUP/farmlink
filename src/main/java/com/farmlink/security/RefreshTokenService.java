package com.farmlink.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refresh:";

    public void save(String email, String refreshToken, long expirationMs) {
        redisTemplate.opsForValue().set(
                PREFIX + email,
                refreshToken,
                Duration.ofMillis(expirationMs)
        );
    }

    public String get(String email) {
        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    public void delete(String email) {
        redisTemplate.delete(PREFIX + email);
    }

    public boolean matches(String email, String refreshToken) {
        String saved = get(email);
        return refreshToken.equals(saved);
    }
}