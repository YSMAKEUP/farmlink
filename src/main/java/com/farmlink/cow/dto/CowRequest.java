package com.farmlink.cow.dto;

import com.farmlink.cow.domain.CowStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CowRequest {

    private String earTagNumber;
    private String name;
    private String breed;
    private LocalDate birthDate;
    private int parity;
    private CowStatus cowStatus;
}

