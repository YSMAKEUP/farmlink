package com.farmlink.breedingRecord.dto;

import com.farmlink.breedingRecord.entity.BreedingRecordEntity;
import com.farmlink.breedingRecord.entity.PregnancyResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Jackson이 Redis 캐시 JSON을 역직렬화할 때 인스턴스를 만들 방법이 필요해서 추가함
public class BreedingResponseDto {

    private Long id;
    private Long cowId;
    private String cowName;
    private LocalDate inseminationDate;
    private String semenCode;
    private String technicianName;
    private String note;
    private LocalDate dueDate;
    private PregnancyResult checkResult;
    private LocalDate checkDate;

    public BreedingResponseDto(BreedingRecordEntity entity) {
        this.id = entity.getId();
        this.cowId = entity.getCow().getId();
        this.cowName = entity.getCow().getName();
        this.inseminationDate = entity.getInseminationDate();
        this.semenCode = entity.getSemenCode();
        this.technicianName = entity.getTechnicianName();
        this.note = entity.getNote();
        this.dueDate = entity.getDueDate();
        this.checkResult = entity.getCheckResult();
        this.checkDate = entity.getCheckDate();
    }
}