package com.farmlink.calendarNote.repository;

import com.farmlink.calendarNote.entity.CalendarNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarNoteRepository extends JpaRepository<CalendarNoteEntity, Long> {

    // 특정 날짜에 해당하는 메모 조회
    List<CalendarNoteEntity> findByMemoDate(LocalDate memoDate);

}