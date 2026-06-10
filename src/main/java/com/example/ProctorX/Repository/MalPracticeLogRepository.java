package com.example.ProctorX.Repository;

import com.example.ProctorX.Entity.ExamSessionEntity;
import com.example.ProctorX.Entity.MalPracticeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MalPracticeLogRepository
        extends JpaRepository<MalPracticeLogEntity, Long> {

    List<MalPracticeLogEntity> findBySession(ExamSessionEntity session);
    long countBySession(ExamSessionEntity session);
    // 🔥 NEW: group by event type
    @Query("""
        SELECT l.eventType, COUNT(l)
        FROM MalPracticeLogEntity l
        WHERE l.session = :session
        GROUP BY l.eventType
    """)
    List<Object[]> countByEventType(ExamSessionEntity session);
    List<MalPracticeLogEntity>
    findBySessionOrderByTimestampAsc(ExamSessionEntity session);

    List<MalPracticeLogEntity> findAllByOrderByTimestampDesc();

}

