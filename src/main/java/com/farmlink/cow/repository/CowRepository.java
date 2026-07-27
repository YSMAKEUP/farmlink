package com.farmlink.cow.repository;

import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.domain.CowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CowRepository extends JpaRepository<CowEntity, Long> {

    boolean existsByEarTagNumber(String earTagNumber);
    Optional<CowEntity> findByEarTagNumber(String earTagNumber);

    @Query("SELECT c FROM CowEntity c " +
            "WHERE (:keyword IS NULL OR c.earTagNumber LIKE %:keyword% OR c.name LIKE %:keyword%) " +
            "AND (:status IS NULL OR c.status = :status)")
    Page<CowEntity> searchCows(
            @Param("keyword") String keyword,
            @Param("status") CowStatus status,
            Pageable pageable
    );
}