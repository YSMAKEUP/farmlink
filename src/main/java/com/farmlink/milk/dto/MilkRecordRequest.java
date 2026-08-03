package com.farmlink.milk.dto;

import com.farmlink.milk.domain.MilkSession;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MilkRecordRequest {

    private Long cowId;
    private LocalDate milkDate;
    private MilkSession session;
    private Double amount;
}