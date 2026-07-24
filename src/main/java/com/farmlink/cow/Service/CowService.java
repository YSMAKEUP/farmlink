package com.farmlink.cow.Service;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.dto.CowRequest;
import com.farmlink.cow.dto.CowResponse;
import com.farmlink.cow.exception.DuplicateEarTagNumberException;
import com.farmlink.cow.repository.CowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CowService {

    private final CowRepository cowRepository;

    @Transactional
    public CowResponse registerCow(CowRequest request) {

        // 1. 비즈니스 규칙 체크 (A안: 사전 체크)
        if (cowRepository.existsByEarTagNumber(request.getEarTagNumber())) {
            throw new DuplicateEarTagNumberException(
                    "이미 등록된 이표번호입니다: " + request.getEarTagNumber()
            );
        }

        // 2. DTO → Entity 변환
        CowEntity cow = CowEntity.builder()
                .earTagNumber(request.getEarTagNumber())
                .name(request.getName())
                .breed(request.getBreed())
                .birthDate(request.getBirthDate())
                .parity(request.getParity())
                .status(request.getStatus())
                .build();

        // 3. 저장 (B안: DB unique 제약이 최종 방어선)
        CowEntity saved = cowRepository.save(cow);

        // 4. Entity → Response 변환
        return CowResponse.from(saved);
    }
}