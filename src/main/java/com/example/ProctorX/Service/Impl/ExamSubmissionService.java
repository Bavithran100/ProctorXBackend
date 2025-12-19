package com.example.ProctorX.Service.Impl;

import com.example.ProctorX.Entity.*;
import com.example.ProctorX.Repository.ExamRepository;
import com.example.ProctorX.Repository.ExamSubmissionRepository;
import com.example.ProctorX.Repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamSubmissionService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamSubmissionRepository submissionRepository;

    // START EXAM
    public ExamEntity startExam(Long examId, AuthEntity student) {

        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));



        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime()) || now.isAfter(exam.getEndTime())) {
            throw new IllegalStateException("EXAM_NOT_ACTIVE");
        }

        return exam;
    }

    // SUBMIT EXAM (entity based)
    public int submitExam(
            Long examId,
            AuthEntity student,
            ExamSubmissionEntity submission
    ) {
        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (submissionRepository.existsByExam_IdAndStudent_Id(examId, student.getId())) {
            throw new IllegalStateException("EXAM_ALREADY_SUBMITTED");
        }

        submission.setExam(exam);
        submission.setStudent(student);
        submission.setSubmittedAt(LocalDateTime.now());

        int score = 0;

        for (AnswerEntity ans : submission.getAnswers()) {

            QuestionEntity question = questionRepository
                    .findById(ans.getQuestion().getId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            ans.setSubmission(submission);
            ans.setQuestion(question);

            if (question.getCorrectOption()
                    .equals(ans.getSelectedOption())) {
                score += question.getMarks();
            }
        }

        submission.setScore(score);

        submissionRepository.save(submission);

        return score;
    }


    public List<ExamSubmissionEntity> getMyResults(String email) {
        return submissionRepository.findByStudentEmailOrderBySubmittedAtDesc(email);
    }
}
