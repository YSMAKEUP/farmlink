package com.farmlink.cow.domain;

import com.farmlink.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

   @Column
   private Enum cowstatus;

   @Column
   private String createdAt;

   @Column
   private String updatedAt;







}
