package com.farmlink.milk.domain;

import com.farmlink.common.BaseTimeEntity;
import com.farmlink.cow.domain.CowEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "milk_records")
public class MilkRecord  extends BaseTimeEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cow_id", nullable = false)
    private CowEntity cow;

    @Column(nullable = false)
    private LocalDate milkedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilkSession session;

    @Column(nullable = false)
    private Double amount;

    @Builder
    public MilkRecord (CowEntity cow, LocalDate milkedDate, MilkSession session, Double amount){
        this.cow = cow;
        this.milkedDate = milkedDate;
        this.session = session;
        this.amount = amount;
    }











}
