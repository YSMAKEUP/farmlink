package com.farmlink.milk.dto;
import com.farmlink.milk.domain.MilkRecord;
import com.farmlink.milk.domain.MilkSession;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MilkRecordResponse {
    private  Long id;
    private Long cowId;
    private String cowName;
    private LocalDate milkedDate;
    private MilkSession session;
    private String sessionLabel;
    private Double amount;

    public static MilkRecordResponse from(MilkRecord milkRecord){
        MilkRecordResponse response = new MilkRecordResponse();
         response.id = milkRecord.getId();
         response.cowId = milkRecord.getCow().getId();
         response.cowName =milkRecord.getCow().getName();
         response.milkedDate =milkRecord.getMilkedDate();
         response.session =milkRecord.getSession();
         response.sessionLabel =milkRecord.getSession().getLabel();
         response.amount = milkRecord.getAmount();

        return  response;
    }


}
