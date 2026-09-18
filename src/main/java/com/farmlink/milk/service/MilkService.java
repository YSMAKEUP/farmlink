package com.farmlink.milk.service;
import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.domain.CowStatus;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.milk.domain.MilkRecord;
import com.farmlink.milk.dto.MilkRecordRequest;
import com.farmlink.milk.dto.MilkRecordResponse;
import com.farmlink.milk.repository.MilkRepository;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 클래스 전체에 readOnly 트랜잭션을 걸어둠 - MilkRecord.cow가 LAZY라서, DTO로 변환하며
// cow.getName()을 호출하는 시점까지 Hibernate 세션이 열려 있어야 함(안 그러면
// LazyInitializationException 발생 - 실제로 이 문제 때문에 GET 요청들이 500/403으로 터졌었음).
public class MilkService {

    private final MilkRepository milkRepository;
    private final CowRepository cowRepository;
    private final UserRepository userRepository;

    private UserEntity resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
    }

    // 실제 그 젖소가 존재하는지, 내 농장 소속이 맞는지, 착유 중인 상태인지 확인 후 등록
    @Transactional
    public MilkRecordResponse registerRecord(MilkRecordRequest request, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();

        CowEntity cow = cowRepository.findById(request.getCowId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소입니다."));

        if (!cow.getRegisteredBy().getFarmCode().equals(farmCode)) {
            throw new IllegalArgumentException("다른 농장의 개체에는 착유기록을 등록할 수 없습니다.");
        }

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

    // 특정 소 목록 조회 - 같은 농장 데이터만
    public List<MilkRecordResponse> getMilkRecordsByCow(Long cowId, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        List<MilkRecord> records = milkRepository.findByCow_RegisteredBy_FarmCodeAndCow_Id(farmCode, cowId);

        return records.stream()
                .map(MilkRecordResponse::from)
                .toList();
    }

    // 날짜 범위 조회 - 같은 농장 데이터만
    public List<MilkRecordResponse> getMilkRecordsByDateRange(LocalDate startDate, LocalDate endDate, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();
        List<MilkRecord> records = milkRepository.findByCow_RegisteredBy_FarmCodeAndMilkedDateBetween(farmCode, startDate, endDate);

        return records.stream()
                .map(MilkRecordResponse::from)
                .toList();
    }
}
