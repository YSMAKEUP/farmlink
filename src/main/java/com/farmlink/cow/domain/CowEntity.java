package com.farmlink.cow.domain;

import com.farmlink.common.BaseTimeEntity;
import com.farmlink.users.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CowEntity extends BaseTimeEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column
   private String earTagNumber;

   @Column
   private String name;

   @Column
   private String breed;

   @Column
   private LocalDate birthDate;

   @Column
   private int parity;

   @Enumerated(EnumType.STRING)
   @Column
   private CowStatus status;

   // 이 개체를 등록한 사용자 - 농장 구분(farmCode)은 여기(registeredBy.farmCode)를 통해서만 판단함.
   // WorkLog/BreedingRecord와 동일하게 "누가 등록했는지"를 농장 소유권 기준으로 삼는 패턴.
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "registered_by", nullable = false)
   private UserEntity registeredBy;

   @Builder
   public CowEntity(String earTagNumber, String name, String breed,
                    LocalDate birthDate, int parity, CowStatus status, UserEntity registeredBy) {
      this.earTagNumber = earTagNumber;
      this.name = name;
      this.breed = breed;
      this.birthDate = birthDate;
      this.parity = parity;
      this.status = status;
      this.registeredBy = registeredBy;
   }
}
