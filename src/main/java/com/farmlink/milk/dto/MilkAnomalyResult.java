package com.farmlink.milk.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

// 착유량 이상감지 판단 결과. Claude API로 리포트 문장을 만드는 건 이 결과를 입력으로 받아
// 이 서비스 밖에서 별도로 처리한다 (이상치 판단 로직과 리포트 생성 로직 분리).
@Getter
@Builder
public class MilkAnomalyResult {

    private final Long cowId;
    private final LocalDate latestDate;
    private final double latestAmount;
    private final double baselineMean;
    private final double baselineStdDev;
    private final boolean anomaly;
    private final boolean insufficientData; // baseline 데이터가 너무 적어 판단을 보류한 경우 true
}
