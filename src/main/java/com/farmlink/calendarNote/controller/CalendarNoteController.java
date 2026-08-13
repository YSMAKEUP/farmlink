package com.farmlink.calendarNote.controller;

import com.farmlink.calendarNote.dto.calendarNoteRequest;
import com.farmlink.calendarNote.dto.CalendarNoteResponse;
import com.farmlink.calendarNote.service.CalendarNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar-notes")
@RequiredArgsConstructor
public class CalendarNoteController {

    private final CalendarNoteService calendarNoteService;

    // 메모 등록
    @PostMapping
    public ResponseEntity<CalendarNoteResponse> createCalendarNote(
            @RequestBody calendarNoteRequest request) {
        CalendarNoteResponse response = calendarNoteService.createCalendarNote(request);
        return ResponseEntity.ok(response);
    }

    // 특정 날짜 메모 조회
    @GetMapping
    public ResponseEntity<List<CalendarNoteResponse>> getNotesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CalendarNoteResponse> responses = calendarNoteService.getNotesByDate(date);
        return ResponseEntity.ok(responses);
    }
}