package com.farmlink.breedingRecord.Service;

import com.farmlink.breedingRecord.Repository.BreedRecordRepository;
import com.farmlink.breedingRecord.dto.BreedingRequestDto;
import com.farmlink.breedingRecord.dto.BreedingResponseDto;
import com.farmlink.breedingRecord.entity.BreedingRecordEntity;
import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return new BreedingResponseDto(saved);
    }

    public List<BreedingResponseDto> getByMonth(int year, int month, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        return breedRecordRepository.findByUserId_FarmCodeAndDueDateBetween(farmCode, start, end)
                .stream()
                .map(BreedingResponseDto::new)
                .toList();
    }

    public List<BreedingResponseDto> getByCow(Long cowId, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        return breedRecordRepository.findByUserId_FarmCodeAndCow_Id(farmCode, cowId)
                .stream()
                .map(BreedingResponseDto::new)
                .toList();
    }
}
