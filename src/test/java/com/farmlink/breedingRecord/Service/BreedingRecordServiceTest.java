package com.farmlink.breedingRecord.Service;

import com.farmlink.breedingRecord.Repository.BreedRecordRepository;
import com.farmlink.breedingRecord.dto.BreedingResponseDto;
import com.farmlink.breedingRecord.entity.BreedingRecordEntity;
import com.farmlink.breedingRecord.entity.PregnancyResult;
import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// BreedingRecordService의 신규 기능(getAll, updateCheckResult)만 단위 테스트로 검증.
// create()는 BreedingRequestDto에 setter/builder가 없어 순수 단위테스트로 만들기 애매해서
// curl로 이미 확인한 부분은 제외하고, 새로 추가한 로직 위주로만 짬.
@ExtendWith(MockitoExtension.class)
class BreedingRecordServiceTest {

    @Mock
    private BreedRecordRepository breedRecordRepository;
    @Mock
    private CowRepository cowRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BreedingRecordService breedingRecordService;

    private UserEntity farmAUser;
    private UserEntity farmBUser;
    private BreedingRecordEntity record;

    @BeforeEach
    void setUp() {
        farmAUser = UserEntity.builder()
                .name("김농부")
                .email("farmer@a.com")
                .password("encoded")
                .farmCode("FARM-A")
                .farmName("A농장")
                .build();

        farmBUser = UserEntity.builder()
                .name("이농부")
                .email("farmer@b.com")
                .password("encoded")
                .farmCode("FARM-B")
                .farmName("B농장")
                .build();

        CowEntity cow = CowEntity.builder()
                .earTagNumber("1234")
                .name("종달이")
                .breed("홀스타인")
                .birthDate(LocalDate.of(2023, 1, 1))
                .parity(1)
                .build();

        record = BreedingRecordEntity.builder()
                .cow(cow)
                .userId(farmAUser)
                .inseminationDate(LocalDate.of(2026, 1, 1))
                .semenCode("KPN-0421")
                .technicianName("김철수")
                .note("")
                .build();
        // 생성 시점에 checkResult=WAITING, checkDate=null, dueDate=인공수정일+280일이 자동으로 세팅됨
    }

    @Test
    void getAll_같은_농장_기록만_조회한다() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(farmAUser));
        when(breedRecordRepository.findByUserId_FarmCode("FARM-A")).thenReturn(List.of(record));

        List<BreedingResponseDto> result = breedingRecordService.getAll(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCowName()).isEqualTo("종달이");
        assertThat(result.get(0).getCheckResult()).isEqualTo(PregnancyResult.WAITING);
    }

    @Test
    void updateCheckResult_결과와_감정일이_바뀐다() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(farmAUser));
        when(breedRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        BreedingResponseDto result = breedingRecordService.updateCheckResult(1L, PregnancyResult.SUCCESS, 10L);

        assertThat(result.getCheckResult()).isEqualTo(PregnancyResult.SUCCESS);
        assertThat(result.getCheckDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void updateCheckResult_WAITING으로_되돌리면_감정일이_null이_된다() {
        record.updateCheckResult(PregnancyResult.SUCCESS); // 먼저 SUCCESS로 만들어둠
        when(userRepository.findById(10L)).thenReturn(Optional.of(farmAUser));
        when(breedRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        BreedingResponseDto result = breedingRecordService.updateCheckResult(1L, PregnancyResult.WAITING, 10L);

        assertThat(result.getCheckResult()).isEqualTo(PregnancyResult.WAITING);
        assertThat(result.getCheckDate()).isNull();
    }

    @Test
    void updateCheckResult_다른_농장_기록이면_예외가_발생한다() {
        // record는 farmAUser 소유인데, farmBUser(FARM-B) 소속 유저(id=20)가 수정 시도
        when(userRepository.findById(20L)).thenReturn(Optional.of(farmBUser));
        when(breedRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> breedingRecordService.updateCheckResult(1L, PregnancyResult.SUCCESS, 20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("다른 농장");
    }
}
