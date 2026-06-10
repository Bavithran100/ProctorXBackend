package com.example.ProctorX.Service.Impl;

import com.example.ProctorX.Config.ExamWarningException;
import com.example.ProctorX.Entity.AuthEntity;
import com.example.ProctorX.Entity.ExamEntity;
import com.example.ProctorX.Entity.ExamSessionEntity;
import com.example.ProctorX.Repository.ExamSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExamSessionService {

    private final ExamSessionRepository sessionRepository;

    public ExamSessionEntity createSession(
            ExamEntity exam,
            AuthEntity student
    ) {
        if (sessionRepository.existsByExamAndStudent(exam, student)) {
            throw new IllegalStateException("SESSION_ALREADY_EXISTS");
        }

        ExamSessionEntity session = new ExamSessionEntity();
        session.setExam(exam);
        session.setStudent(student);
        session.setStartTime(LocalDateTime.now());
        session.setLastHeartbeat(LocalDateTime.now());
        session.setStatus(ExamSessionEntity.Status.ACTIVE);

        return sessionRepository.save(session);
    }

    public void markSubmitted(ExamEntity exam, AuthEntity student) {
        ExamSessionEntity session = sessionRepository
                .findByExamAndStudent(exam, student)
                .orElseThrow();
        if (session.getStatus() == ExamSessionEntity.Status.TERMINATED) {
            throw new RuntimeException("EXAM_TERMINATED");
        }


        session.setStatus(ExamSessionEntity.Status.SUBMITTED);
        sessionRepository.save(session);
    }
    public void heartbeat(ExamEntity exam, AuthEntity student) {

        ExamSessionEntity session = sessionRepository
                .findByExamAndStudent(exam, student)
                .orElseThrow(() -> new RuntimeException("SESSION_NOT_FOUND"));



        if (session.getStatus() == ExamSessionEntity.Status.LOCKED) {
            throw new IllegalStateException("SESSION_LOCKED");
        }

        if (session.getStatus() == ExamSessionEntity.Status.TERMINATED) {
            throw new IllegalStateException("SESSION_TERMINATED");
        }
        System.out.println(session.getWarning());
        if (session.getWarning() != null && !session.getWarning().isBlank()) {
            String warning=session.getWarning();
            session.setWarning(null);
            throw new ExamWarningException(warning);
        }



        session.setLastHeartbeat(LocalDateTime.now());
        sessionRepository.save(session);

    }

}
