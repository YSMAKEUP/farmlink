package com.farmlink.breedingRecord.dto;

import com.farmlink.breedingRecord.entity.BreedingRecordEntity;
import com.farmlink.breedingRecord.entity.PregnancyResult;
import lombok.Getter;

import java.time.LocalDate;

@Getter
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