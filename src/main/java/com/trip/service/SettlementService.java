package com.trip.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trip.dto.SettlementDTO;
import com.trip.entity.Expense;
import com.trip.entity.TripMember;
import com.trip.entity.User;
import com.trip.repository.ExpenseRepository;
import com.trip.repository.TripMemberRepository;
import com.trip.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {

    @Autowired
    private ExpenseRepository expenseRepo;

    @Autowired
    private TripMemberRepository memberRepo;

    @Autowired
    private UserRepository userRepo;

    public List<SettlementDTO> settle(Long tripId) {

        List<Expense> expenses = expenseRepo.findByTripId(tripId);
        List<TripMember> members = memberRepo.findByTripId(tripId);

        // ✅ SAFETY CHECK
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Double> paidMap = new HashMap<>();

        // initialize members
        for (TripMember m : members) {
            paidMap.put(m.getUserId(), 0.0);
        }

        double total = 0;

        // sum expenses
        for (Expense e : expenses) {
            paidMap.put(
                    e.getPaidBy(),
                    paidMap.getOrDefault(e.getPaidBy(), 0.0) + e.getAmount()
            );
            total += e.getAmount();
        }

        double share = total / members.size();

        // calculate balance
        Map<Long, Double> balance = new HashMap<>();
        for (Long userId : paidMap.keySet()) {
            balance.put(userId, paidMap.get(userId) - share);
        }

        List<Map.Entry<Long, Double>> creditors = new ArrayList<>();
        List<Map.Entry<Long, Double>> debtors = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : balance.entrySet()) {
            if (entry.getValue() > 0) creditors.add(entry);
            else if (entry.getValue() < 0) debtors.add(entry);
        }

        // ✅ SORT (important for correct settlement)
        creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        debtors.sort((a, b) -> Double.compare(a.getValue(), b.getValue()));

        List<SettlementDTO> result = new ArrayList<>();

        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {

            Long debtorId = debtors.get(i).getKey();
            Long creditorId = creditors.get(j).getKey();

            double debt = -debtors.get(i).getValue();
            double credit = creditors.get(j).getValue();

            // ✅ ROUNDING FIX
            double amount = Math.round(Math.min(debt, credit) * 100.0) / 100.0;

            User fromUser = userRepo.findById(debtorId).orElse(null);
            User toUser = userRepo.findById(creditorId).orElse(null);

            if (fromUser == null || toUser == null) {
                continue;
            }

            // ✅ NEW DTO (WITH IDs)
            result.add(new SettlementDTO(
                    debtorId,
                    creditorId,
                    fromUser.getName(),
                    toUser.getName(),
                    amount
            ));

            // update balances
            debtors.get(i).setValue(debtors.get(i).getValue() + amount);
            creditors.get(j).setValue(creditors.get(j).getValue() - amount);

            if (Math.abs(debtors.get(i).getValue()) < 0.01) i++;
            if (Math.abs(creditors.get(j).getValue()) < 0.01) j++;
        }

        return result;
    }
}