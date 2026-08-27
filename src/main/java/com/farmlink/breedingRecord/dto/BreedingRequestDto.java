package com.farmlink.breedingRecord.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BreedingRequestDto {

    private Long cowId;
    private LocalDate inseminationDate;
    private String semenCode;
    private String technicianName;
    private String note;
}