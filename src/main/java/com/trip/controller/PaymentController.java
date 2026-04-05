package com.trip.controller;

import com.trip.dto.SettlementDTO;
import com.trip.entity.Payment;
import com.trip.repository.PaymentRepository;
import com.trip.service.RazorpayService;
import com.trip.service.SettlementService;
import com.trip.service.TripService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@CrossOrigin("*")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private SettlementService settlementService;
    @Autowired
    private TripService tripService;

    // ✅ 1. CREATE ORDER (RAZORPAY)
    @PostMapping("/create-order")
    public String createOrder(@RequestParam Double amount) {
        return razorpayService.createOrder(amount);
    }

    // ✅ 2. CREATE PAYMENTS FROM SETTLEMENT 🔥
    @PostMapping("/create/{tripId}")
    public void createPayments(@PathVariable Long tripId) {

        // 🔥 REMOVE OLD FIRST
        paymentRepo.deleteByTripId(tripId);

        List<SettlementDTO> settlements = settlementService.settle(tripId);

        for (SettlementDTO s : settlements) {

            Payment p = new Payment();
            p.setTripId(tripId);
            p.setFromUserId(s.getFromUserId());
            p.setToUserId(s.getToUserId());
            p.setAmount(s.getAmount());
            p.setPaid(false);

            paymentRepo.save(p);
        }
    }

    // ✅ 3. GET PAYMENTS (VERY IMPORTANT 🔥)
    @GetMapping("/{tripId}")
    public List<Payment> getPayments(@PathVariable Long tripId) {
        return paymentRepo.findByTripId(tripId);
    }

    // ✅ 4. MARK PAYMENT AS PAID
    @PostMapping("/pay/{paymentId}")
    public void markPaid(@PathVariable Long paymentId) {
    	System.out.println("🔥 Payment API HIT");
        Payment p = paymentRepo.findById(paymentId).orElseThrow();

        // ✅ mark as paid
        p.setPaid(true);
        paymentRepo.save(p);

        // 🔥 SEND EMAIL TO ALL MEMBERS
        tripService.sendPaymentEmail(
                p.getTripId(),
                p.getFromUserId(),
                p.getToUserId(),
                p.getAmount()
        );
    }
    
    @GetMapping("/user/{userId}")
    public List<Payment> getUserPayments(@PathVariable Long userId) {
        return paymentRepo.findByFromUserIdOrToUserId(userId, userId);
    }
    
}