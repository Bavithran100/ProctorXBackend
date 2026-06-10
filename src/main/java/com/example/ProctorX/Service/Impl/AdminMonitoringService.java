package com.example.ProctorX.Service.Impl;

import com.example.ProctorX.Entity.ExamSessionEntity;
import com.example.ProctorX.Repository.ExamSessionRepository;
import com.example.ProctorX.Repository.MalPracticeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMonitoringService {

    private final ExamSessionRepository sessionRepository;
    private final MalPracticeLogRepository logRepository;
    public List<Map<String, Object>> getLiveSessions() {

        List<ExamSessionEntity> sessions =
                sessionRepository.findByStatus(
                        ExamSessionEntity.Status.ACTIVE
                );

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> response = new ArrayList<>();

        for (ExamSessionEntity session : sessions) {

            long malpracticeCount =
                    logRepository.countBySession(session);

            boolean inactive =
                    session.getLastHeartbeat() == null ||
                            session.getLastHeartbeat()
                                    .isBefore(now.minusSeconds(30));

            int riskScore = calculateRiskScore(malpracticeCount, inactive);

            // 🔥 NEW: event-wise breakdown
            Map<String, Long> eventBreakdown = new HashMap<>();

            List<Object[]> groupedEvents =
                    logRepository.countByEventType(session);

            for (Object[] row : groupedEvents) {
                String eventType = row[0].toString();
                Long count = (Long) row[1];
                eventBreakdown.put(eventType, count);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", session.getId());
            data.put("studentId", session.getStudent().getId());
            data.put("studentName", session.getStudent().getName());
            data.put("examId", session.getExam().getId());
            data.put("examTitle", session.getExam().getTitle());
            data.put("startTime", session.getStartTime());
            data.put("lastHeartbeat", session.getLastHeartbeat());
            data.put("status", session.getStatus());

            data.put("malpracticeCount", malpracticeCount);
            data.put("riskScore", riskScore);
            data.put("inactive", inactive);

            // 🔥 ADD THIS
            data.put("events", eventBreakdown);

            response.add(data);
        }

        return response;
    }


    private int calculateRiskScore(
            long malpracticeCount,
            boolean inactive
    ) {
        int score = (int) malpracticeCount;
        if (inactive) score += 3;
        return score;
    }
}
