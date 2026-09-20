package com.farmlink.milk.service;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.milk.domain.MilkRecord;
import com.farmlink.milk.dto.MilkAnomalyResult;
import com.farmlink.milk.repository.MilkRepository;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 착유량 이상감지 - 최근 WINDOW_DAYS일 중 오늘을 제외한 baseline 평균 ± THRESHOLD_K * 표준편차를
// 벗어나면 이상치로 판단한다. 리포트 문장(Claude API 호출)은 이 결과를 받아 서비스 밖에서 처리.
// 표준편차는 표본표준편차(n-1)로 계산 - baseline을 "그 소의 정상 범위를 추정하는 샘플"로 보는 관점 (9/18 확정)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilkAnomalyService {

    static final int WINDOW_DAYS = 14;       // 최근 14일 (9/18 결정)
    static final double THRESHOLD_K = 2.0;   // 평균 ± 2*표준편차 (9/18 결정)
    static final int MIN_BASELINE_DAYS = 7;  // 9/18 확정: 표준편차가 안정적으로 계산될 최소 일수(윈도우 절반)

    private final MilkRepository milkRepository;
    private final CowRepository cowRepository;
    private final UserRepository userRepository;

    private UserEntity resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
    }

    public MilkAnomalyResult detectAnomaly(Long cowId, Long userId) {
        UserEntity user = resolveUser(userId);

        CowEntity cow = cowRepository.findById(cowId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소입니다."));

        if (!cow.getRegisteredBy().getFarmCode().equals(user.getFarmCode())) {
            throw new IllegalArgumentException("다른 농장의 개체는 조회할 수 없습니다.");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(WINDOW_DAYS - 1);

        List<MilkRecord> records = milkRepository.findByCow_RegisteredBy_FarmCodeAndCow_IdAndMilkedDateBetween(
                user.getFarmCode(), cowId, startDate, today);

        // 세션(아침/저녁)별로 따로 들어오는 기록을 날짜 단위 총 착유량으로 합산
        Map<LocalDate, Double> dailyTotals = records.stream()
                .collect(Collectors.groupingBy(MilkRecord::getMilkedDate, Collectors.summingDouble(MilkRecord::getAmount)));

        double latestAmount = dailyTotals.getOrDefault(today, 0.0);

        List<Double> baseline = dailyTotals.entrySet().stream()
                .filter(entry -> !entry.getKey().isEqual(today))
                .map(Map.Entry::getValue)
                .toList();

        if (baseline.size() < MIN_BASELINE_DAYS) {
            return MilkAnomalyResult.builder()
                    .cowId(cowId)
                    .latestDate(today)
                    .latestAmount(latestAmount)
                    .baselineMean(0.0)
                    .baselineStdDev(0.0)
                    .anomaly(false)
                    .insufficientData(true)
                    .build();
        }

        double mean = baseline.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double sumSquaredDiff = baseline.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum();
        double variance = sumSquaredDiff / (baseline.size() - 1); // 표본표준편차 (n-1)
        double stdDev = Math.sqrt(variance);

        boolean isAnomaly = Math.abs(latestAmount - mean) > THRESHOLD_K * stdDev;

        return MilkAnomalyResult.builder()
                .cowId(cowId)
                .latestDate(today)
                .latestAmount(latestAmount)
                .baselineMean(mean)
                .baselineStdDev(stdDev)
                .anomaly(isAnomaly)
                .insufficientData(false)
                .build();
    }
}
