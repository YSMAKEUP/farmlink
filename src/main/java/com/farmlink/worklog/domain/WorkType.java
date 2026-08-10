package com.farmlink.worklog.domain;

import lombok.Getter;

@Getter
public enum WorkType {
    FEED("사료"),
    TREATMENT("치료"),
    QUARANTINE("방역"),
    ETC("기타");

    private final String label;

    WorkType(String label){
        this.label = label;
    }

}
