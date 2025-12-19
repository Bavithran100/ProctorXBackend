

package com.example.ProctorX.Entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "exams")
@Getter
@Setter
public class ExamEntity {
    public enum ExamStatus {
        DRAFT,      // created but not visible
        PUBLISHED,  // visible & startable (within time)
        DISABLED    // admin manually disabled
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private int duration; // minutes

    private int totalMarks;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 🔥 NEW: Admin control
    @Enumerated(EnumType.STRING)
    private ExamStatus status; // DRAFT, PUBLISHED, DISABLED

    @OneToOne(mappedBy = "exam", cascade = CascadeType.ALL)
    @JsonManagedReference("exam-instruction")
    private ExamInstructionEntity instruction;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    @JsonManagedReference("exam-questions")
    private List<QuestionEntity> questions = new ArrayList<>();
}
