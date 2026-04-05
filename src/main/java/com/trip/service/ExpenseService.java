package com.trip.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.trip.dto.SettlementDTO;
import com.trip.entity.Expense;
import com.trip.entity.ExpenseCategory;
import com.trip.entity.Trip;
import com.trip.entity.TripMember;
import com.trip.entity.User;
import com.trip.repository.ExpenseParticipantRepository;
import com.trip.repository.ExpenseRepository;
import com.trip.repository.TripMemberRepository;
import com.trip.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final ExpenseParticipantRepository participantRepo;
    private final TripService tripService;
    private final TripMemberRepository memberRepo;
    private final UserRepository userRepository;

    // ✅ DELETE ALL EXPENSES BY TRIP
    @Transactional
    public void deleteAllExpenses(Long tripId) {
        expenseRepo.deleteByTripId(tripId);
    }

    // ✅ ADD EXPENSE
    public void addExpense(Long tripId, Long paidBy, Double amount, String desc, ExpenseCategory category) {

        Expense exp = new Expense();

        // 🔥 IMPORTANT FIX → use Trip object instead of tripId
        Trip trip = new Trip();
        trip.setId(tripId);

        exp.setTrip(trip);
        exp.setPaidBy(paidBy);
        exp.setAmount(amount);
        exp.setDescription(desc);
        exp.setCategory(category);

        expenseRepo.save(exp);

        // Update trip total
        tripService.addExpense(tripId, amount);
    }

    // ✅ GET EXPENSES
    public List<Expense> getTripExpenses(Long tripId) {
        return expenseRepo.findByTripId(tripId);
    }

    // ✅ SETTLEMENT LOGIC
    public List<SettlementDTO> calculateSettlement(Long tripId) {

        List<Expense> expenses = expenseRepo.findByTripId(tripId);
        List<TripMember> members = memberRepo.findByTripId(tripId);

        int n = members.size();
        if (n == 0) return new ArrayList<>();

        // Total amount
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double share = total / n;

        // Paid map
        Map<Long, Double> paidMap = new HashMap<>();

        for (TripMember m : members) {
            paidMap.put(m.getUserId(), 0.0);
        }

        for (Expense e : expenses) {
            paidMap.put(e.getPaidBy(),
                    paidMap.getOrDefault(e.getPaidBy(), 0.0) + e.getAmount());
        }

        // Balance map
        Map<Long, Double> balance = new HashMap<>();
        for (Long userId : paidMap.keySet()) {
            balance.put(userId, paidMap.get(userId) - share);
        }

        // Creditors & Debtors
        List<Map.Entry<Long, Double>> creditors = new ArrayList<>();
        List<Map.Entry<Long, Double>> debtors = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : balance.entrySet()) {
            if (entry.getValue() > 0)
                creditors.add(entry);
            else if (entry.getValue() < 0)
                debtors.add(entry);
        }

        List<SettlementDTO> result = new ArrayList<>();

        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {

            Long debtorId = debtors.get(i).getKey();
            double debt = -debtors.get(i).getValue();

            Long creditorId = creditors.get(j).getKey();
            double credit = creditors.get(j).getValue();

            double amount = Math.round(Math.min(debt, credit) * 100.0) / 100.0;

            User fromUser = userRepository.findById(debtorId).orElse(null);
            User toUser = userRepository.findById(creditorId).orElse(null);

            result.add(new SettlementDTO(
                    debtorId,
                    creditorId,
                    fromUser != null ? fromUser.getName() : "Unknown",
                    toUser != null ? toUser.getName() : "Unknown",
                    amount
            ));

            debtors.get(i).setValue(-(debt - amount));
            creditors.get(j).setValue(credit - amount);

            if (Math.abs(debtors.get(i).getValue()) < 0.01) i++;
            if (Math.abs(creditors.get(j).getValue()) < 0.01) j++;
        }

        return result;
    }

    public List<SettlementDTO> getSettlement(Long tripId) {
        return calculateSettlement(tripId);
    }

    public List<Expense> getExpensesByUser(Long userId) {
        return expenseRepo.findByPaidBy(userId);
    }
}