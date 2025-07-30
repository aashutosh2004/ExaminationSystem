package com.example.ExaminationSystem.controller;

import com.example.ExaminationSystem.model.Exam;
import com.example.ExaminationSystem.model.User;
import com.example.ExaminationSystem.service.ExamService;
import com.example.ExaminationSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamApiController {

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    // ✅ 1. Get ALL exams – accessible to all users
    @GetMapping("/all")
    public List<Exam> getAllExams() {
        return examService.getAllExams();
    }

    // ✅ 2. Get exams by educator ID – accessible to all users
    @GetMapping("/educator/{educatorId}")
    public List<Exam> getExamsByEducator(@PathVariable Long educatorId) {
        User educator = userService.getUserById(educatorId)
                .orElseThrow(() -> new IllegalArgumentException("Educator not found"));
        return examService.getExamsByCreator(educator);
    }

    // ✅ 3. Get a specific exam by ID – accessible to all users
    @GetMapping("/{examId}")
    public Exam getExam(@PathVariable Long examId) {
        return examService.getExamById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam ID not found: " + examId));
    }

    // ✅ 4. Create a new exam – restricted to educators only
    @PreAuthorize("hasAuthority('EDUCATOR')")
    @PostMapping("/create")
    public Exam createExam(@RequestBody Exam exam) {
        if (exam.getCreatedBy() == null || exam.getCreatedBy().getId() == null) {
            throw new IllegalArgumentException("Educator (createdBy) is required");
        }

        User educator = userService.getUserById(exam.getCreatedBy().getId())
                .orElseThrow(() -> new IllegalArgumentException("Educator not found"));

        exam.setCreatedBy(educator);
        return examService.createExam(exam);
    }

    // ✅ 5. Update an exam – restricted to educators only
    @PreAuthorize("hasAuthority('EDUCATOR')")
    @PutMapping("/update")
    public Exam updateExam(@RequestBody Exam exam) {
        if (exam.getId() == null) {
            throw new IllegalArgumentException("Exam ID is required");
        }

        Exam existing = examService.getExamById(exam.getId())
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        if (exam.getCreatedBy() == null || !existing.getCreatedBy().getId().equals(exam.getCreatedBy().getId())) {
            throw new IllegalArgumentException("You are not authorized to update this exam");
        }

        return examService.updateExam(exam);
    }

    // ✅ 6. Delete an exam by ID – restricted to educators only
    @PreAuthorize("hasAuthority('EDUCATOR')")
    @DeleteMapping("/delete/{examId}")
    public String deleteExam(@PathVariable Long examId) {
        examService.deleteExam(examId);
        return "Deleted exam with ID: " + examId;
    }
}
