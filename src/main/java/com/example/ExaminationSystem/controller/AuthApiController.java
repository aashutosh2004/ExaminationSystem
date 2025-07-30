package com.example.ExaminationSystem.controller;

import com.example.ExaminationSystem.dto.LoginRequest;
import com.example.ExaminationSystem.dto.LoginResponse;
import com.example.ExaminationSystem.model.User;
import com.example.ExaminationSystem.repository.UserRepository;
import com.example.ExaminationSystem.security.CustomUserDetails;
import com.example.ExaminationSystem.util.JwtUtil;
import com.example.ExaminationSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// Allow all origins for simplicity, adjust as needed
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
    public class AuthApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok("Registration successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed. User may already exist.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userService.login(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            CustomUserDetails userDetails = new CustomUserDetails(user);
            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new LoginResponse(token, user.getRole(), user.getUsername()));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
//    @GetMapping("/count")
//    public ResponseEntity<Long> getUserCount() {
//        long count = userRepository.count();
//        return ResponseEntity.ok(count);
//    }

}
