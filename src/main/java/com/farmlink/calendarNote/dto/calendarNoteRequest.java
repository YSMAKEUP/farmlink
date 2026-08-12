package com.farmlink.calendarNote.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class calendarNoteRequest {

    private String title;
    private Long cowId;
    private LocalDate memoDate;
    private String content;
}