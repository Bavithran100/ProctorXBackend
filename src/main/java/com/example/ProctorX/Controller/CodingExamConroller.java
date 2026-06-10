package com.example.ProctorX.Controller;

import com.example.ProctorX.Entity.ExamEntity;
import com.example.ProctorX.Repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
public class CodingExamConroller {
    @Autowired
    private final ExamRepository examRepository;
    @GetMapping("/{examId}/coding-questions")
    public ResponseEntity<?> getCodingQuestions(
            @PathVariable Long examId
    ) {

        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow();

        return ResponseEntity.ok(exam.getCodingQuestions());
    }
}
