package com.farmlink.cow.Service;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.dto.CowRequest;
import com.farmlink.cow.dto.CowResponse;
import com.farmlink.cow.dto.CowSearchCondition;
import com.farmlink.cow.dto.PageResponse;
import com.farmlink.cow.exception.DuplicateEarTagNumberException;
import com.farmlink.cow.repository.CowRepository;
import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CowService {

    private final CowRepository cowRepository;
    private final UserRepository userRepository;

    // 인증된 사용자 조회 (없으면 토큰은 유효한데 DB에 유저가 없는 이상 상태 -> 400)
    private UserEntity resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
    }

    @Transactional
    public CowResponse registerCow(CowRequest request, Long userId) {
        UserEntity user = resolveUser(userId);

        // 1. 비즈니스 규칙 체크 (A안: 사전 체크) - 이표번호 중복은 같은 농장 안에서만 체크
        if (cowRepository.existsByEarTagNumberAndRegisteredBy_FarmCode(request.getEarTagNumber(), user.getFarmCode())) {
            throw new DuplicateEarTagNumberException(
                    "이미 등록된 이표번호입니다: " + request.getEarTagNumber()
            );
        }

        // 2. DTO → Entity 변환 (등록자를 registeredBy로 저장 - 이걸로 농장 소유권이 결정됨)
        CowEntity cow = CowEntity.builder()
                .earTagNumber(request.getEarTagNumber())
                .name(request.getName())
                .breed(request.getBreed())
                .birthDate(request.getBirthDate())
                .parity(request.getParity())
                .status(request.getStatus())
                .registeredBy(user)
                .build();

        // 3. 저장 (B안: DB unique 제약이 최종 방어선)
        CowEntity saved = cowRepository.save(cow);

        // 4. Entity → Response 변환
        return CowResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<CowResponse> findCows(CowSearchCondition condition, Pageable pageable, Long userId) {
        String farmCode = resolveUser(userId).getFarmCode();

        Page<CowEntity> page = cowRepository.searchCows(
                farmCode,
                condition.getKeyword(),
                condition.getStatus(),
                pageable
        );

        Page<CowResponse> responsePage = page.map(CowResponse::from);

        return PageResponse.from(responsePage);
    }
}
