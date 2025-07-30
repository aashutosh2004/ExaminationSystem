package com.example.ExaminationSystem.controller;

import com.example.ExaminationSystem.model.Exam;
import com.example.ExaminationSystem.model.User;
import com.example.ExaminationSystem.service.ExamService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class ExamController {

    @Autowired
    private ExamService examService;

    // STUDENT dashboard - show all exams
    @GetMapping("student/dashboard")
    public String studentDashboard(Model model) {
        List<Exam> exams = examService.getAllExams();
        model.addAttribute("exams", exams);
        return "student-dashboard";
    }

    // EDUCATOR dashboard - show exams created by logged-in educator
    @GetMapping("educator/dashboard")
    public String educatorDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"EDUCATOR".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";  // unauthorized or no session -> back to home/login
        }

        List<Exam> exams = examService.getExamsByCreator(user);
        model.addAttribute("exams", exams);
        return "educator-dashboard";
    }

    // Show form to create new exam
    @GetMapping("educator/exam/new")
    public String newExamForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"EDUCATOR".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";
        }
        model.addAttribute("exam", new Exam());
        return "create-exam";
    }

    // Save new exam
    @PostMapping("educator/exam/save")
    public String saveExam(@ModelAttribute Exam exam, HttpSession session) {
        User creator = (User) session.getAttribute("user");
        if (creator == null || !"EDUCATOR".equalsIgnoreCase(creator.getRole())) {
            return "redirect:/";
        }
        exam.setCreatedBy(creator);
        examService.createExam(exam);
        return "redirect:/educator/dashboard";
    }

    // Show form to edit existing exam
    @GetMapping("educator/exam/edit/{id}")
    public String editExam(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"EDUCATOR".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";
        }
        Exam exam = examService.getExamById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id:" + id));
        if (!exam.getCreatedBy().getId().equals(user.getId())) {
            // prevent editing others' exams
            return "redirect:/educator/dashboard";
        }
        model.addAttribute("exam", exam);
        return "create-exam";
    }

    // Update exam
    @PostMapping("educator/exam/update")
    public String updateExam(@ModelAttribute Exam exam, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"EDUCATOR".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";
        }
        // Optional: check if the user owns this exam before updating (security)
        Exam existing = examService.getExamById(exam.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id:" + exam.getId()));
        if (!existing.getCreatedBy().getId().equals(user.getId())) {
            return "redirect:/educator/dashboard";
        }
        exam.setCreatedBy(user);
        examService.updateExam(exam);
        return "redirect:/educator/dashboard";
    }

    // Delete exam
    @GetMapping("educator/exam/delete/{id}")
    public String deleteExam(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"EDUCATOR".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";
        }
        Exam exam = examService.getExamById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam Id:" + id));
        if (!exam.getCreatedBy().getId().equals(user.getId())) {
            return "redirect:/educator/dashboard";
        }
        examService.deleteExam(id);
        return "redirect:/educator/dashboard";
    }
}
