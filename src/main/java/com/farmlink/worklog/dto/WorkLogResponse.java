package com.farmlink.worklog.dto;
import com.farmlink.worklog.domain.WorkLogEntity;
import com.farmlink.worklog.domain.WorkType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorkLogResponse {

    private Long id;
    private LocalDateTime workDateTime;
    private WorkType workType;
    private String workTypeLabel;
    private Long cowId;
    private String cowName;
    private String userName;
    private String content;`


    public WorkLogResponse(WorkLogEntity entity) {
        this.id = entity.getId();
        this.workDateTime = entity.getWorkDateTime();
        this.workType = entity.getWorkType();
        this.workTypeLabel = entity.getWorkType().getLabel();
        this.content = entity.getContent();
        this.userName = entity.getUserId().getName();

        if (entity.getCow() != null) {
            this.cowId = entity.getCow().getId();
            this.cowName = entity.getCow().getName();
        }
    }
}
