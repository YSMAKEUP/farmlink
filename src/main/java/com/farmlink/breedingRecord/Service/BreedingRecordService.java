package com.farmlink.breedingRecord.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmlink.breedingRecord.Repository.BreedRecordRepository;
import com.farmlink.breedingRecord.dto.BreedingRequestDto;
import com.farmlink.breedingRecord.dto.BreedingResponseDto;
import com.farmlink.breedingRecord.entity.BreedingRecordEntity;
import com.farmlink.breedingRecord.entity.PregnancyResult;
import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BreedingRecordService {

    private final BreedRecordRepository breedRecordRepository;
    private final CowRepository cowRepository;
    private final UserRepository userRepository;
    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper cacheObjectMapper;

    private static final String REDIS_MONTH_PREFIX = "breeding:month:";
    private static final Duration REDIS_MONTH_TTL = Duration.ofMinutes(30);

    private CowEntity resolveCow(Long cowId) {
        return cowRepository.findById(cowId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 개체입니다. id=" + cowId));
    }

    private UserEntity resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
    }

    @Transactional
    public BreedingResponseDto create(BreedingRequestDto dto, Long userId) {
        CowEntity cow = resolveCow(dto.getCowId());
        UserEntity user = resolveUser(userId);

        BreedingRecordEntity entity = BreedingRecordEntity.builder()
                .cow(cow)
                .userId(user)
                .inseminationDate(dto.getInseminationDate())
                .semenCode(dto.getSemenCode())
                .technicianName(dto.getTechnicianName())
                .note(dto.getNote())
                .build();

        BreedingRecordEntity saved = breedRecordRepository.save(entity);

        // 새 기록의 분만예정일(dueDate)이 속한 달의 캐시는 이제 낡은 데이터라 지워줌
        if (saved.getDueDate() != null) {
            evictMonthCache(user.getFarmCode(), saved.getDueDate());
        }

        return new BreedingResponseDto(saved);
    }

    public List<BreedingResponseDto> getByMonth(int year, int month, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        String cacheKey = monthCacheKey(farmCode, year, month);

        // 1. 로컬 캐시(Caffeine) 확인 — 있으면 DB/Redis 둘 다 안 타고 바로 반환
        Object cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            return (List<BreedingResponseDto>) cached;
        }

        // 2. Redis 확인 — 다른 서버 인스턴스가 이미 캐시해놨을 수도 있음
        String redisValue = redisTemplate.opsForValue().get(REDIS_MONTH_PREFIX + cacheKey);
        if (redisValue != null) {
            List<BreedingResponseDto> fromRedis = deserialize(redisValue);
            localCache.put(cacheKey, fromRedis);
            return fromRedis;
        }

        // 3. 캐시 둘 다 없으면 DB 조회
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<BreedingResponseDto> result = breedRecordRepository.findByUserId_FarmCodeAndDueDateBetween(farmCode, start, end)
                .stream()
                .map(BreedingResponseDto::new)
                .toList();

        // 4. 조회 결과로 로컬/Redis 캐시 둘 다 채워둠
        localCache.put(cacheKey, result);
        redisTemplate.opsForValue().set(REDIS_MONTH_PREFIX + cacheKey, serialize(result), REDIS_MONTH_TTL);

        return result;
    }

    public List<BreedingResponseDto> getByCow(Long cowId, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        return breedRecordRepository.findByUserId_FarmCodeAndCow_Id(farmCode, cowId)
                .stream()
                .map(BreedingResponseDto::new)
                .toList();
    }

    public List<BreedingResponseDto> getAll(Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        return breedRecordRepository.findByUserId_FarmCode(farmCode)
                .stream()
                .map(BreedingResponseDto::new)
                .toList();
    }

    @Transactional
    public BreedingResponseDto updateCheckResult(Long id, PregnancyResult result, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        BreedingRecordEntity entity = breedRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 번식 기록입니다. id=" + id));

        if (!entity.getUserId().getFarmCode().equals(farmCode)) {
            throw new IllegalArgumentException("다른 농장의 기록은 수정할 수 없습니다.");
        }

        entity.updateCheckResult(result);

        // 임신감정 결과가 바뀌면 캘린더에 보이는 값도 바뀌어야 하니 해당 월 캐시 무효화
        if (entity.getDueDate() != null) {
            evictMonthCache(farmCode, entity.getDueDate());
        }

        return new BreedingResponseDto(entity);
    }

    private String monthCacheKey(String farmCode, int year, int month) {
        return farmCode + ":" + year + "-" + month;
    }

    private void evictMonthCache(String farmCode, LocalDate date) {
        String cacheKey = monthCacheKey(farmCode, date.getYear(), date.getMonthValue());
        localCache.invalidate(cacheKey);
        redisTemplate.delete(REDIS_MONTH_PREFIX + cacheKey);
    }

    private String serialize(List<BreedingResponseDto> list) {
        try {
            return cacheObjectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("캐시 직렬화 실패", e);
        }
    }

    private List<BreedingResponseDto> deserialize(String json) {
        try {
            return cacheObjectMapper.readValue(json, new TypeReference<List<BreedingResponseDto>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("캐시 역직렬화 실패", e);
        }
    }
}
