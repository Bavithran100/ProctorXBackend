package com.example.ProctorX.Service.Impl;

import com.example.ProctorX.Entity.ExamEntity;
import com.example.ProctorX.Entity.QuestionEntity;
import com.example.ProctorX.Repository.ExamRepository;
import com.example.ProctorX.Repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public ExamEntity createExam(ExamEntity exam) {

        // Link instruction to exam
        if (exam.getInstruction() != null) {
            exam.getInstruction().setExam(exam);
        }
        ZoneId ist = ZoneId.of("Asia/Kolkata");
        LocalDateTime istStart = exam.getStartTime();
        LocalDateTime istEnd = exam.getEndTime();
        exam.setStartTime(
                istStart.atZone(ist).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
        );

        exam.setEndTime(
                istEnd.atZone(ist).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
        );
        exam.setStatus(ExamEntity.ExamStatus.DRAFT); // or PUBLISHED later
        examRepository.save(exam);
        return exam;
    }

    public QuestionEntity addQuestion(Long examId, QuestionEntity question) {

        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        question.setExam(exam);
        return questionRepository.save(question);
    }
    public String publish(Long examId){
        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        exam.setStatus(ExamEntity.ExamStatus.PUBLISHED);
        examRepository.save(exam);
        return "Success";


    }
}
