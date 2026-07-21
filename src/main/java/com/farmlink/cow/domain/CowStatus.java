package com.farmlink.cow.domain;

public enum CowStatus {
    HEIFER,   // 미경산우 (출산 경험 없음)
    MILKING,  // 착유중
    DRY,      // 건유 (분만 전 착유 중단기)
    SOLD,     // 판매
    DEAD      // 폐사
}