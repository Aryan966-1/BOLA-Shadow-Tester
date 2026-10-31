package com.bolaguard.demo.controller;

import com.bolaguard.demo.dto.AuthRequest;
import com.bolaguard.demo.dto.AuthResponse;
import com.bolaguard.demo.entity.User;
import com.bolaguard.demo.repository.UserRepository;
import com.bolaguard.demo.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Log in as a demo user",
        description = "Demo shortcut: enter alice, bob, charlie, or admin. No password is checked. Copy the returned token, click Authorize, and paste the token only; Swagger adds the Bearer prefix.",
        requestBody = @RequestBody(required = true, content = @Content(examples = {
            @ExampleObject(name = "Alice — owner of file 101", value = "{\"username\":\"alice\"}"),
            @ExampleObject(name = "Bob — viewer of file 101", value = "{\"username\":\"bob\"}"),
            @ExampleObject(name = "Charlie — no access to file 101", value = "{\"username\":\"charlie\"}")
        })),
        responses = {
            @ApiResponse(responseCode = "200", description = "JWT issued."),
            @ApiResponse(responseCode = "401", description = "Unknown demo username.")
        }
    )
    public ResponseEntity<AuthResponse> login(@org.springframework.web.bind.annotation.RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
