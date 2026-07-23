package com.farmlink.cow.dto;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.domain.CowStatus;

import java.time.LocalDate;

public class CowResponse {

    private Long id;
    private String earTagNumber;
    private String name;
    private String breed;
    private LocalDate birthDate;
    private int parity;
    private CowStatus cowStatus;
    private String createdAt;
    private String updateAt;

    public static CowResponse from(CowEntity entity){
        CowResponse response = new CowResponse();
        response.id = entity.getId();
        response.earTagNumber = entity.getEarTagNumber();
        response.breed =entity.getBreed();
        response.name = entity.getName();
        response.cowStatus =entity.getStatus();
        response.birthDate = entity.getBirthDate();
        response.parity = entity.getParity();


        return response;
    }

}
