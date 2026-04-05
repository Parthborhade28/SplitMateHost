package com.trip.service;

import com.trip.dto.*;
import com.trip.entity.User;
import com.trip.repository.UserRepository;
import com.trip.security.JwtUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 🔥 REGISTER
    public User registerUser(AuthRequest req) {

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setUpiId(req.getUpiId());

        return userRepo.save(user);
    }

    // 🔥 LOGIN USER (NO EXCEPTION)
    public User loginUser(AuthRequest req) {

        User user = userRepo.findByEmail(req.getEmail()).orElse(null);

        if (user == null) {
            return null; // 🔥 controller will handle 404
        }

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            return null; // 🔥 controller will handle 401
        }

        return user;
    }

    // 🔥 TOKEN
    public String generateToken(String email) {
        return jwtUtil.generateToken(email);
    }
}