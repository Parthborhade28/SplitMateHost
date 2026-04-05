package com.trip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trip.dto.ExpenseRequest;
import com.trip.dto.SettlementDTO;
import com.trip.dto.TripRequest;
import com.trip.entity.Trip;
import com.trip.entity.TripMember;
import com.trip.service.ExpenseService;
import com.trip.service.TripService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173"
})
public class TripController {

    @Autowired
    private TripService tripService;
    
    @Autowired
    private ExpenseService expenseService;
  
    // ✅ CREATE TRIP (VALIDATION ENABLED)
    @PostMapping
    public Trip createTrip(@RequestBody @Valid TripRequest req) {
        return tripService.createTrip(req.getName(), req.getUserId());
    }

    // ✅ GET MY TRIPS
    @GetMapping("/my")
    public List<Trip> myTrips(@RequestParam Long userId) {
        return tripService.getMyTrips(userId);
    }

    // ✅ ADD MEMBER
    @PostMapping("/{tripId}/add-member")
    public String addMember(@PathVariable Long tripId,
                            @RequestParam Long userId) {

        tripService.addMember(tripId, userId);

        return "Member added";
    }

    // ✅ GET MEMBERS
    @GetMapping("/{tripId}/members")
    public List<TripMember> getMembers(@PathVariable Long tripId) {
        return tripService.getMembers(tripId);
    }

    // ✅ FINISH TRIP + EMAIL
    @PostMapping("/finish/{tripId}")
    public String finishTrip(@PathVariable Long tripId) {
        tripService.finishTripAndSendEmail(tripId);
        return "Trip finished and emails sent!";
    }

    // 🔥 NEW: GET SETTLEMENT (VERY IMPORTANT)
    @GetMapping("/{tripId}/settlement")
    public List<SettlementDTO> getSettlement(@PathVariable Long tripId) {
        return tripService.calculateSettlement(tripId);
    }
    
    @DeleteMapping("/{tripId}/remove-member/{userId}")
    public String removeMember(@PathVariable Long tripId,
                               @PathVariable Long userId) {

        tripService.removeMember(tripId, userId);
        return "Member removed!";
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.ok("Trip deleted successfully");
    }
    
    
}