package com.farmlink.breedingRecord.dto;

import com.farmlink.breedingRecord.entity.PregnancyResult;
import lombok.Getter;

@Getter
public class BreedingCheckRequestDto {
    private PregnancyResult checkResult;
}