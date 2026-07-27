package com.farmlink.cow.dto;

import com.farmlink.cow.domain.CowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CowSearchCondition {
    private String keyword;   // 이표번호 또는 이름 부분 검색, 없으면 null
    private CowStatus status; // 전체 상태 조회면 null
}