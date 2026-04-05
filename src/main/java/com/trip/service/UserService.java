package com.trip.service;

import com.trip.entity.User;
import com.trip.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TripMemberRepository memberRepo;

    @Autowired
    private ExpenseRepository expenseRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    // 🔥 DELETE USER
    public void deleteUserByEmail(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found ❌"));

        Long userId = user.getId();

        // 🔥 DELETE RELATED DATA

        // remove from trips
        memberRepo.deleteByUserId(userId);

        // delete expenses
        expenseRepo.deleteByPaidBy(userId);

        // delete payments
        paymentRepo.deleteByFromUserIdOrToUserId(userId, userId);

        // delete user
        userRepo.delete(user);
    }
}