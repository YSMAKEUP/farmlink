package com.farmlink.cow.domain;

import com.farmlink.common.BaseTimeEntity;
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

   @Builder
   public CowEntity(String earTagNumber, String name, String breed,
                    LocalDate birthDate, int parity, CowStatus status) {
      this.earTagNumber = earTagNumber;
      this.name = name;
      this.breed = breed;
      this.birthDate = birthDate;
      this.parity = parity;
      this.status = status;
   }
}