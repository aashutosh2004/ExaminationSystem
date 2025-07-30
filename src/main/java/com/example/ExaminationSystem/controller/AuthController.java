package com.example.ExaminationSystem.controller;

import com.example.ExaminationSystem.model.User;
import com.example.ExaminationSystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "index"; // index.html contains login & register forms in one page
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            model.addAttribute("message", "Registered successfully! Please login.");
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. User may already exist.");
        }
        return "index";
    }


    @PostMapping("/login")
    public String login(@RequestParam String usernameOrEmail,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        return userService.login(usernameOrEmail, password)
                .map(user -> {
                    session.setAttribute("user", user);
                    if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                        return "redirect:/student/dashboard";
                    } else if ("EDUCATOR".equalsIgnoreCase(user.getRole())) {
                        return "redirect:/educator/dashboard";
                    } else {
                        session.invalidate();
                        model.addAttribute("error", "Unknown user role.");
                        return "index";
                    }
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid credentials!");
                    return "index";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    @GetMapping("/login")
    public String loginPage() {
        return "login";  // login.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";  // register.html
    }

}
