package com.farmlink.cow.repository;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.domain.CowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CowRepository extends JpaRepository<CowEntity, Long> {

    // 이표번호 중복 체크는 농장 단위로만 유효함 (다른 농장은 같은 번호를 써도 됨)
    boolean existsByEarTagNumberAndRegisteredBy_FarmCode(String earTagNumber, String farmCode);
    Optional<CowEntity> findByEarTagNumber(String earTagNumber);

    @EntityGraph(attributePaths = {"registeredBy"})
    @Query("SELECT c FROM CowEntity c " +
            "WHERE c.registeredBy.farmCode = :farmCode " +
            "AND (:keyword IS NULL OR c.earTagNumber LIKE %:keyword% OR c.name LIKE %:keyword%) " +
            "AND (:status IS NULL OR c.status = :status)")
    Page<CowEntity> searchCows(
            @Param("farmCode") String farmCode,
            @Param("keyword") String keyword,
            @Param("status") CowStatus status,
            Pageable pageable
    );
}
