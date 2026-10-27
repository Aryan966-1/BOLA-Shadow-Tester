package com.bolaguard.demo.controller;

import com.bolaguard.demo.dto.AuthRequest;
import com.bolaguard.demo.dto.AuthResponse;
import com.bolaguard.demo.entity.User;
import com.bolaguard.demo.repository.UserRepository;
import com.bolaguard.demo.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername()));
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
