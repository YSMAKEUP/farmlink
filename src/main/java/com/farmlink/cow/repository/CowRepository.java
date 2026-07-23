package com.farmlink.cow.repository;

import com.farmlink.cow.domain.CowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CowRepository extends JpaRepository<CowEntity,Long>{
    boolean existsByEarTagNumber(String earTagNumber);
    Optional<CowEntity> findByEarTagNumber(String earTagNumber);


}
