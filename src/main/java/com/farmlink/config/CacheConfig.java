package com.farmlink.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    // 애플리케이션 메모리에 바로 두는 로컬(1차) 캐시.
    // 여러 도메인에서 공용으로 쓸 수 있게 범용 Cache<String, Object>로 둠.
    @Bean
    public Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }

    // Redis에 DTO를 JSON 문자열로 저장/복원할 때 쓰는 전용 ObjectMapper.
    // 응답 DTO들이 기본 생성자/setter가 없는 경우가 많아서, 필드에 직접 접근하도록 설정해야
    // Jackson이 역직렬화를 할 수 있음. LocalDate 등 자바 8 시간 타입 지원을 위해 JavaTimeModule도 등록.
    @Bean
    public ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
