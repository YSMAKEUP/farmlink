package com.farmlink.calendarNote.entity;

import com.farmlink.common.BaseTimeEntity;
import com.farmlink.cow.domain.CowEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class CalendarNoteEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연결할 소 (선택)
    @ManyToOne
    private CowEntity cow;

    // 메모가 속한 날짜
    private LocalDate memoDate;

    private String title;

    private String content;

    @Builder
    public CalendarNoteEntity(CowEntity cow, LocalDate memoDate, String title, String content) {
        this.cow = cow;
        this.memoDate = memoDate;
        this.title = title;
        this.content = content;
    }
}