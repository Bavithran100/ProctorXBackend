
package com.example.ProctorX.Controller;
import com.example.ProctorX.Config.ExamWarningException;
import com.example.ProctorX.Entity.AuthEntity;
import com.example.ProctorX.Entity.ExamEntity;
import com.example.ProctorX.Entity.ExamSubmissionEntity;
import com.example.ProctorX.Entity.MalPracticeLogEntity;
import com.example.ProctorX.Repository.ExamRepository;
import com.example.ProctorX.Service.AuthService;
import com.example.ProctorX.Service.Impl.ExamSessionService;
import com.example.ProctorX.Service.Impl.ExamSubmissionService;
import com.example.ProctorX.Service.Impl.MalPracticeLogService;
import com.example.ProctorX.Service.Impl.StudentExamService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
public class StudentExamController {
    @Autowired
     private final ExamRepository examRepository;
@Autowired
    private final StudentExamService studentExamService;

    @GetMapping("/today")
    public ResponseEntity<List<ExamEntity>> getTodaysExams(Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

//        if (isAdmin) {
//            // Admin sees all published exams
//            List<ExamEntity> allPublished = examRepository.findAll().stream()
//                    .filter(e -> e.getStatus() == ExamEntity.ExamStatus.DRAFT)
//                    .toList();
//            return ResponseEntity.ok(allPublished);
//        } else {
            // Students see today's exams
            return ResponseEntity.ok(studentExamService.getTodaysExams());

    }

    @GetMapping("/upcoming")
    public List<ExamEntity> upcomingExams() {
        return studentExamService.getUpcomingExams();
    }
    private final ExamSessionService examSessionService;
    private final ExamSubmissionService submissionService;
    private final AuthService authService;

    // START EXAM
    @GetMapping("/{examId}/start")
    public ResponseEntity<?> startExam(
            @PathVariable Long examId,
            Authentication authentication,
            HttpSession session
    ) {
        try {
            AuthEntity student = authService.getCurrentUser(authentication);
            ExamEntity exam = submissionService.startExam(examId, student);
            ExamEntity exam1 = examRepository.findById(examId)
                    .orElseThrow();

          examSessionService.createSession(exam1, student);

            // store start time ONCE
            String key = "EXAM_START_" + exam1.getId();

            if (session.getAttribute(key) == null) {
                session.setAttribute(key, LocalDateTime.now());
            }

            return ResponseEntity.ok(exam);


        }

        catch (IllegalStateException e) {
            if ("EXAM_ALREADY_SUBMITTED".equals(e.getMessage())) {
                return ResponseEntity.status(409).body("Exam already submitted");
            }
            return ResponseEntity.status(403).body("Exam not active");
        }



    }
    private boolean isTimeOver(
            ExamEntity exam,
            HttpSession session
    ) {
        LocalDateTime start =
                (LocalDateTime) session.getAttribute("EXAM_START_" + exam.getId());

        if (start == null) return true;

        LocalDateTime end =
                start.plusMinutes(exam.getDuration());

        return LocalDateTime.now().isAfter(end);
    }


    // SUBMIT EXAM (ENTITY)
    @PostMapping("/{examId}/submit")
    public ResponseEntity<?> submitExam(
            @PathVariable Long examId,
            @RequestBody ExamSubmissionEntity submission,
            Authentication authentication
    ) {
        try {
            ExamEntity exam = examRepository.findById(examId)
                    .orElseThrow();
            AuthEntity student = authService.getCurrentUser(authentication);







        Integer score = submissionService.submitExam(
                examId,
                student,
                submission
        );
        examSessionService.markSubmitted(exam, student);

            return ResponseEntity.ok(
                    Map.of("score", score)
            );}


        catch (IllegalStateException e) {
            if ("EXAM_ALREADY_SUBMITTED".equals(e.getMessage())) {
                return ResponseEntity.status(409).body("Exam already submitted");
            }
            return ResponseEntity.status(403).body("Exam not active");
        }

    }
    @PostMapping("/{examId}/heartbeat")
    public ResponseEntity<?> heartbeat(
            @PathVariable Long examId,
            Authentication authentication,
            HttpSession session
    ) {
        AuthEntity student = authService.getCurrentUser(authentication);

        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow();
        if (isTimeOver(exam, session)) {
            return ResponseEntity
                    .status(403)
                    .body("TIME_OVER");
        }


        try {
            examSessionService.heartbeat(exam, student);
            return ResponseEntity.ok().build();

        }
        catch (ExamWarningException e) {
            // ⚠️ WARNING — exam continues
            return ResponseEntity
                    .status(200)
                    .header("X-EXAM-WARNING", "true")
                    .body(e.getMessage());

        }

        catch (IllegalStateException e) {
            return ResponseEntity.status(403).body("Session not active");
        }
    }
    @Autowired
  private   MalPracticeLogService malPracticeLogService;
    @PostMapping("/{examId}/malpractice")
    public ResponseEntity<?> logMalpractice(
            @PathVariable Long examId,
            @RequestParam String event,
            Authentication authentication
    ) {
        AuthEntity student = authService.getCurrentUser(authentication);

        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow();

        MalPracticeLogEntity.EventType eventType =
                MalPracticeLogEntity.EventType.valueOf(event);

        malPracticeLogService.logEvent(exam, student, eventType);

        return ResponseEntity.ok().build();
    }


}
