package com.farmlink.milk.service;
import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.domain.CowStatus;
import com.farmlink.cow.dto.CowRequest;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.milk.domain.MilkRecord;
import com.farmlink.milk.dto.MilkRecordRequest;
import com.farmlink.milk.dto.MilkRecordResponse;
import com.farmlink.milk.repository.MilkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MilkService {

    //착유기록이 필요하니까 어떤 게 필요한걸까? 착유기록,젖소 정보,
    //1. 그 소가 실제로 존재하는지. 어느 상태이냐도 확인 가능해야할듯.
    private final MilkRepository milkRepository;
    private final CowRepository cowRepository;

    // 실제 그 젖소가 존재하는지 파악을 해보기.
    public MilkRecordResponse registerRecord(MilkRecordRequest request) {
        CowEntity cow = cowRepository.findById(request.getCowId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소입니다."));


        if (cow.getStatus() != CowStatus.MILKING) {
            throw new IllegalArgumentException("착유 중인 소가 아닙니다: " + cow.getStatus());
        }

        MilkRecord milkRecord = MilkRecord.builder()
                .cow(cow)
                .milkedDate(request.getMilkDate())
                .session(request.getSession())
                .amount(request.getAmount())
                .build();


        MilkRecord saved = milkRepository.save(milkRecord);

        return MilkRecordResponse.from(saved);


    }

    //특정 소 목록 조회
    public List<MilkRecordResponse>getMilkRecordsByCow(Long cowId){
       List<MilkRecord> records =  milkRepository.findByCow_Id(cowId);


        return records.stream()
                .map(MilkRecordResponse::from)
                .toList();

    }

    //날짜 범위 조회
    public List<MilkRecordResponse>getMilkRecordsByDateRange(LocalDate startDate, LocalDate endDate){
        List<MilkRecord> records = milkRepository.findByMilkedDateBetween(startDate,endDate);

        return records.stream()
                .map(MilkRecordResponse::from)
                .toList();
    }


}
