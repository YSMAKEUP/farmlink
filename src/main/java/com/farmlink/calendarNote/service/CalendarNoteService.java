package com.farmlink.calendarNote.service;

import com.farmlink.calendarNote.dto.calendarNoteRequest;
import com.farmlink.calendarNote.dto.CalendarNoteResponse;
import com.farmlink.calendarNote.entity.CalendarNoteEntity;
import com.farmlink.calendarNote.repository.CalendarNoteRepository;
import com.farmlink.cow.domain.CowEntity;
import com.farmlink.cow.repository.CowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarNoteService {

    private final CalendarNoteRepository calendarNoteRepository;
    private final CowRepository cowRepository;

    // 메모 등록
    public CalendarNoteResponse createCalendarNote(calendarNoteRequest request) {
        CowEntity cow = resolveCow(request.getCowId());

        CalendarNoteEntity note = CalendarNoteEntity.builder()
                .cow(cow)
                .memoDate(request.getMemoDate())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        CalendarNoteEntity saved = calendarNoteRepository.save(note);
        return new CalendarNoteResponse(saved);
    }

    // 특정 날짜 조회
    public List<CalendarNoteResponse> getNotesByDate(LocalDate date) {
        return calendarNoteRepository.findByMemoDate(date)
                .stream()
                .map(CalendarNoteResponse::new)
                .collect(Collectors.toList());
    }

    // cowId가 있으면 존재 검증 후 반환, 없으면 null (소 없는 일반 메모 허용)
    private CowEntity resolveCow(Long cowId) {
        if (cowId == null) {
            return null;
        }
        return cowRepository.findById(cowId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소입니다. id=" + cowId));
    }
}