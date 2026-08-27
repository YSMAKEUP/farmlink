package com.farmlink.breedingRecord.entity;

import com.farmlink.common.BaseTimeEntity;
import com.farmlink.cow.domain.CowEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BreedingRecordEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CowEntity cow;

    private LocalDate inseminationDate;

    private String semenCode;

    private String technicianName;

    private String note;

    @Builder
    public BreedingRecordEntity(CowEntity cow, LocalDate inseminationDate, String semenCode,
                                 String technicianName, String note) {
        this.cow = cow;
        this.inseminationDate = inseminationDate;
        this.semenCode = semenCode;
        this.technicianName = technicianName;
        this.note = note;
    }
}
