package com.example.ProctorX.Service.Impl;

import com.example.ProctorX.Entity.AdminActionEntity;
import com.example.ProctorX.Entity.AuthEntity;
import com.example.ProctorX.Entity.ExamSessionEntity;
import com.example.ProctorX.Repository.AdminActionRepository;
import com.example.ProctorX.Repository.ExamSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminActionService {

    private final ExamSessionRepository sessionRepository;
    private final AdminActionRepository actionRepository;

    public void performAction(
            Long sessionId,
            AuthEntity admin,
            AdminActionEntity.ActionType action,
            String remark
    ) {
        ExamSessionEntity session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SESSION_NOT_FOUND"));

        // Enforce action
        switch (action) {
            case LOCK -> session.setStatus(ExamSessionEntity.Status.LOCKED);
            case TERMINATE -> session.setStatus(ExamSessionEntity.Status.TERMINATED);
            case WARN -> {
                session.setWarning(remark);
                // no session state change
            }
        }

        sessionRepository.save(session);

        // Audit log
        AdminActionEntity log = new AdminActionEntity();
        log.setAdmin(admin);
        log.setSession(session);
        log.setAction(action);
        log.setRemark(remark);
        log.setTimestamp(LocalDateTime.now());

        actionRepository.save(log);
    }
}
