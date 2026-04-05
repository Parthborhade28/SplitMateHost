package com.trip.controller;

import com.trip.dto.*;
import com.trip.entity.User;
import com.trip.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {
	@Autowired
	AuthService authService;

	@PostMapping("/register")
	public AuthResponse register(@RequestBody AuthRequest req){

	    User user = authService.registerUser(req); // 🔥 get user

	    String token = authService.generateToken(user.getEmail());

	    return new AuthResponse(token, user.getId()); // ✅ FIXED
	}

	@PostMapping("/login")
	public AuthResponse login(@RequestBody AuthRequest req){

	    User user = authService.loginUser(req);

	    String token = authService.generateToken(user.getEmail());

	    return new AuthResponse(token, user.getId());  // ✅ correct
	}

}
