package com.farmlink.worklog.dto;
import com.farmlink.milk.domain.MilkRecord;
import com.farmlink.milk.dto.MilkRecordResponse;
import com.farmlink.worklog.domain.WorkLogEntity;
import com.farmlink.worklog.domain.WorkType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorklogRequest {

    private Long cowId;
    private WorkType workType;
    private LocalDateTime workDateTime;
    private String content;
}
