package com.farmlink.breedingRecord.entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.farmlink.common.BaseTimeEntity;
import com.farmlink.cow.domain.CowEntity;
import com.farmlink.users.domain.UserEntity;
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

    @ManyToOne
    private UserEntity userId;

    private LocalDate inseminationDate;

    private String semenCode;

    private String technicianName;

    private String note;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private PregnancyResult checkResult;

    private LocalDate checkDate;

    public void updateCheckResult(PregnancyResult checkResult){
        this.checkResult  =checkResult;
        this.checkDate = checkResult== PregnancyResult.WAITING ? null : LocalDate.now();
    }

    @Builder
    public BreedingRecordEntity(CowEntity cow, UserEntity userId, LocalDate inseminationDate, String semenCode,
                                 String technicianName, String note) {
        this.cow = cow;
        this.userId = userId;
        this.inseminationDate = inseminationDate;
        this.semenCode = semenCode;
        this.technicianName = technicianName;
        this.note = note;
        this.dueDate = inseminationDate != null ? inseminationDate.plusDays(280) : null;
        this.checkResult = PregnancyResult.WAITING;
    }


}
