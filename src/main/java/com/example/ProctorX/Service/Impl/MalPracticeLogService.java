package com.example.ProctorX.Service.Impl;

import com.example.ProctorX.Entity.AuthEntity;
import com.example.ProctorX.Entity.ExamEntity;
import com.example.ProctorX.Entity.ExamSessionEntity;
import com.example.ProctorX.Entity.MalPracticeLogEntity;
import com.example.ProctorX.Repository.ExamSessionRepository;
import com.example.ProctorX.Repository.MalPracticeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MalPracticeLogService {

    private final ExamSessionRepository sessionRepository;
    private final MalPracticeLogRepository logRepository;

    public void logEvent(
            ExamEntity exam,
            AuthEntity student,
            MalPracticeLogEntity.EventType eventType
    ) {
        ExamSessionEntity session = sessionRepository
                .findByExamAndStudent(exam, student)
                .orElseThrow(() -> new RuntimeException("SESSION_NOT_FOUND"));

        if (session.getStatus() != ExamSessionEntity.Status.ACTIVE) {
            return; // ignore events if session not active
        }

        MalPracticeLogEntity log = new MalPracticeLogEntity();
        log.setSession(session);
        log.setEventType(eventType);
        log.setSeverity(mapSeverity(eventType));
        log.setTimestamp(LocalDateTime.now());

        logRepository.save(log);
    }

    private MalPracticeLogEntity.Severity mapSeverity(
            MalPracticeLogEntity.EventType eventType
    ) {
        return switch (eventType) {
            case TAB_SWITCH, WINDOW_BLUR -> MalPracticeLogEntity.Severity.LOW;
            case COPY, PASTE, RIGHT_CLICK -> MalPracticeLogEntity.Severity.MEDIUM;
            case PAGE_REFRESH -> MalPracticeLogEntity.Severity.HIGH;
        };
    }
}
