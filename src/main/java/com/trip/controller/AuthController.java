package com.trip.controller;

import com.trip.dto.*;
import com.trip.entity.User;
import com.trip.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    // 🔥 REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {

        try {
            User user = authService.registerUser(req);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Registration failed ❌");
            }

            String token = authService.generateToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(token, user.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong ❌");
        }
    }

    // 🔥 LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {

        try {
            User user = authService.loginUser(req);

            // ❌ USER NOT FOUND
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found ❌");
            }

            String token = authService.generateToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(token, user.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials ❌");
        }
    }
}