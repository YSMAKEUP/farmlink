package com.farmlink.calendarNote.dto;

import com.farmlink.calendarNote.entity.CalendarNoteEntity;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CalendarNoteResponse {
    private Long id;
    private String title;
    private Long cowId;
    private LocalDate memoDate;
    private String content;

    public CalendarNoteResponse(CalendarNoteEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.cowId = entity.getCow() != null ? entity.getCow().getId() : null;
        this.memoDate = entity.getMemoDate();
        this.content = entity.getContent();
    }
}