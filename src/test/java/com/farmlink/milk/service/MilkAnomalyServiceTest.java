package com.farmlink.milk.service;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.domain.CowStatus;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.milk.domain.MilkRecord;
import com.farmlink.milk.domain.MilkSession;
import com.farmlink.milk.dto.MilkAnomalyResult;
import com.farmlink.milk.repository.MilkRepository;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.when;

// 착유량 이상감지(최근 14일 평균 ± 2*표준편차 기준) 판단 로직만 단위테스트로 검증.
// Claude API로 리포트 문장을 생성하는 부분은 이 서비스 밖에서 별도로 처리 - 여기선 통계 판단까지만.
// baseline 일수가 MIN_BASELINE_DAYS(7일) 미만이면 판단을 보류함 (9/18 확정).
// 표준편차는 표본표준편차(n-1)로 계산함 (9/18 확정).
@ExtendWith(MockitoExtension.class)
class MilkAnomalyServiceTest {

    @Mock
    private MilkRepository milkRepository;
    @Mock
    private CowRepository cowRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MilkAnomalyService milkAnomalyService;

    private UserEntity farmAUser;
    private UserEntity farmBUser;
    private CowEntity cow;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        farmAUser = UserEntity.builder()
                .name("김농부").email("farmer@a.com").password("encoded")
                .farmCode("FARM-A").farmName("A농장")
                .build();

        farmBUser = UserEntity.builder()
                .name("이농부").email("farmer@b.com").password("encoded")
                .farmCode("FARM-B").farmName("B농장")
                .build();

        cow = CowEntity.builder()
                .earTagNumber("1234").name("종달이").breed("홀스타인")
                .birthDate(LocalDate.of(2023, 1, 1)).parity(1)
                .status(CowStatus.MILKING)
                .registeredBy(farmAUser)
                .build();
    }

    // 하루 총 착유량(dailyTotal)을 아침/저녁 두 세션에 절반씩 나눠 담은 레코드 생성
    private List<MilkRecord> dailyRecords(LocalDate date, double dailyTotal) {
        return List.of(
                MilkRecord.builder().cow(cow).milkedDate(date).session(MilkSession.MORNING).amount(dailyTotal / 2).build(),
                MilkRecord.builder().cow(cow).milkedDate(date).session(MilkSession.EVENING).amount(dailyTotal / 2).build()
        );
    }

    private void stubRecords(List<MilkRecord> records) {
        when(milkRepository.findByCow_RegisteredBy_FarmCodeAndCow_IdAndMilkedDateBetween(
                "FARM-A", 100L, today.minusDays(MilkAnomalyService.WINDOW_DAYS - 1), today))
                .thenReturn(records);
    }

    @Test
    void detectAnomaly_최근값이_평균에서_2표준편차_넘게_벗어나면_이상치로_판단한다() {
        List<MilkRecord> records = new ArrayList<>();
        // baseline 13일: 29.0/31.0 번갈아 -> 평균 30, 표준편차 약 1
        for (int i = MilkAnomalyService.WINDOW_DAYS - 1; i >= 1; i--) {
            double amount = (i % 2 == 0) ? 29.0 : 31.0;
            records.addAll(dailyRecords(today.minusDays(i), amount));
        }
        records.addAll(dailyRecords(today, 15.0)); // 오늘 급감 - 평균보다 훨씬 낮음

        when(userRepository.findById(1L)).thenReturn(Optional.of(farmAUser));
        when(cowRepository.findById(100L)).thenReturn(Optional.of(cow));
        stubRecords(records);

        MilkAnomalyResult result = milkAnomalyService.detectAnomaly(100L, 1L);

        assertThat(result.isAnomaly()).isTrue();
        assertThat(result.getLatestAmount()).isEqualTo(15.0);
        assertThat(result.getBaselineMean()).isCloseTo(30.0, offset(0.1));
    }

    @Test
    void detectAnomaly_정상범위면_이상치가_아니다() {
        List<MilkRecord> records = new ArrayList<>();
        for (int i = MilkAnomalyService.WINDOW_DAYS - 1; i >= 0; i--) {
            double amount = (i % 2 == 0) ? 29.0 : 31.0; // 오늘도 같은 패턴 유지
            records.addAll(dailyRecords(today.minusDays(i), amount));
        }

        when(userRepository.findById(1L)).thenReturn(Optional.of(farmAUser));
        when(cowRepository.findById(100L)).thenReturn(Optional.of(cow));
        stubRecords(records);

        MilkAnomalyResult result = milkAnomalyService.detectAnomaly(100L, 1L);

        assertThat(result.isAnomaly()).isFalse();
    }

    @Test
    void detectAnomaly_다른_농장_소이면_예외가_발생한다() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(farmBUser));
        when(cowRepository.findById(100L)).thenReturn(Optional.of(cow)); // cow는 FARM-A 소속

        assertThatThrownBy(() -> milkAnomalyService.detectAnomaly(100L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("다른 농장");
    }

    @Test
    void detectAnomaly_기준일수가_부족하면_판단을_보류한다() {
        List<MilkRecord> records = new ArrayList<>();
        records.addAll(dailyRecords(today.minusDays(1), 30.0)); // baseline 1일뿐
        records.addAll(dailyRecords(today, 5.0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(farmAUser));
        when(cowRepository.findById(100L)).thenReturn(Optional.of(cow));
        stubRecords(records);

        MilkAnomalyResult result = milkAnomalyService.detectAnomaly(100L, 1L);

        assertThat(result.isInsufficientData()).isTrue();
        assertThat(result.isAnomaly()).isFalse();
    }
}
