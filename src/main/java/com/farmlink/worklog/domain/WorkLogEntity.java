package com.farmlink.worklog.domain;

import com.farmlink.common.BaseTimeEntity;
import com.farmlink.cow.domain.CowEntity;
import com.farmlink.users.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
public class WorkLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //사용자 id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userId;

    //대상 개체
    @ManyToOne
    private CowEntity cow;

    //작업 종류
    @Enumerated(EnumType.STRING)
    private WorkType workType;

    //작업일지
    private LocalDateTime workDateTime;

    //작업내용
    private String content;

    @Builder
    public WorkLogEntity(LocalDateTime workDateTime, WorkType workType, CowEntity cow, String content) {
        this.workDateTime = workDateTime;
        this.workType = workType;
        this.cow = cow;
        this.content = content;
    }
}