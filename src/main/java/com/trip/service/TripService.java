package com.trip.service;

import com.trip.dto.SettlementDTO;
import com.trip.entity.*;
import com.trip.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Transactional
@Service
@RequiredArgsConstructor
public class TripService {

    @Autowired
    private TripRepository tripRepo;

    @Autowired
    private TripMemberRepository memberRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ExpenseRepository expenseRepo;

    @Autowired
    private QRService qrService;
    
    
    
    public List<Trip> getMyTrips(Long userId) {
        return tripRepo.findByCreatedBy(userId);
    }
    public void addMember(Long tripId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean exists = memberRepo.existsByTripIdAndUserId(tripId, userId);

        if (exists) return;

        TripMember member = new TripMember();
        member.setTripId(tripId);
        member.setUserId(userId);

        memberRepo.save(member);

        // 🔥 DEBUG
        System.out.println("Member saved: " + userId);
    }
    public List<TripMember> getMembers(Long tripId) {
        return memberRepo.findByTripId(tripId);
    }

    public Trip createTrip(String name, Long userId) {

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Trip name is required ❌");
        }

        Trip trip = new Trip();
        trip.setName(name);
        trip.setCreatedBy(userId);
        trip.setTotalExpense(0.0);

        Trip saved = tripRepo.save(trip);
        memberRepo.save(new TripMember(null, saved.getId(), userId));

        return saved;
    }
    
    public void removeMember(Long tripId, Long userId) {

        boolean hasExpense = expenseRepo.existsByTripIdAndPaidBy(tripId, userId);

        if (hasExpense) {
            throw new RuntimeException("User has expenses ❌");
        }

        memberRepo.deleteByTripIdAndUserId(tripId, userId);
    }

    public Trip getTripById(Long id) {
        return tripRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
    }

    // 🔥 MAIN METHOD
    public void finishTripAndSendEmail(Long tripId) {

        Trip trip = getTripById(tripId);
        
        List<Expense> expenses = expenseRepo.findByTripId(tripId);

        if (expenses.isEmpty()) {
            throw new RuntimeException("No expenses in trip ❌");
        }
        
        String subject = "Trip Finished - " + trip.getName();

        List<SettlementDTO> settlements = calculateSettlement(tripId);

        Map<String, byte[]> qrImages = new HashMap<>();

        for (SettlementDTO s : settlements) {

            User toUser = userRepository.findById(s.getToUserId()).orElse(null);

            if (toUser == null || toUser.getUpiId() == null) continue;

            String upiLink = "upi://pay?pa=" + toUser.getUpiId()
                    + "&pn=" + toUser.getName()
                    + "&am=" + s.getAmount()
                    + "&cu=INR";

            String cid = "qr_" + s.getToUserId();

            qrImages.put(cid, qrService.generateQRCodeBytes(upiLink));
        }

        String body = buildSummary(trip, settlements);

        List<TripMember> members = memberRepo.findByTripId(tripId);

        for (TripMember member : members) {
            User user = userRepository.findById(member.getUserId()).orElse(null);

            if (user != null) {
                emailService.sendTripSummary(user.getEmail(), subject, body, qrImages);
            }
        }
    }

    // 🔥 SETTLEMENT LOGIC
    public List<SettlementDTO> calculateSettlement(Long tripId) {

        List<Expense> expenses = expenseRepo.findByTripId(tripId);
        List<TripMember> members = memberRepo.findByTripId(tripId);

        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }

        int n = members.size();

        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double share = total / n;

        Map<Long, Double> paidMap = new HashMap<>();

        for (TripMember m : members) {
            paidMap.put(m.getUserId(), 0.0);
        }

        for (Expense e : expenses) {
            paidMap.put(e.getPaidBy(),
                    paidMap.getOrDefault(e.getPaidBy(), 0.0) + e.getAmount());
        }

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

            String fromName = fromUser != null ? fromUser.getName() : "Unknown";
            String toName = toUser != null ? toUser.getName() : "Unknown";

            result.add(new SettlementDTO(
                    debtorId,
                    creditorId,
                    fromName,
                    toName,
                    amount
            ));

            debtors.get(i).setValue(-(debt - amount));
            creditors.get(j).setValue(credit - amount);

            if (Math.abs(debtors.get(i).getValue()) < 0.01) i++;
            if (Math.abs(creditors.get(j).getValue()) < 0.01) j++;
        }

        return result;
    }

    // 🔥 EMAIL BUILDER
    private String buildSummary(Trip trip, List<SettlementDTO> settlements) {

        List<Expense> expenses = expenseRepo.findByTripId(trip.getId());

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<body style='margin:0;background:#f4f6f8;font-family:Arial;'>")

            .append("<div style='max-width:700px;margin:30px auto;background:#ffffff;border-radius:14px;"
                    + "box-shadow:0 10px 30px rgba(0,0,0,0.1);overflow:hidden;'>")

            // 🔥 HEADER
            .append("<div style='background:linear-gradient(135deg,#4f46e5,#6366f1);padding:20px;color:white;text-align:center;'>")
            .append("<h2 style='margin:0;'>🎉 Trip Finished - ").append(trip.getName()).append("</h2>")
            .append("</div>")

            // 🔥 CONTENT
            .append("<div style='padding:25px;'>")

            .append("<p style='font-size:16px;'><b>Total Expense:</b> ₹")
            .append(trip.getTotalExpense()).append("</p>")

            .append("<hr style='border:none;border-top:1px solid #eee;margin:20px 0;'>")

            // 🔥 EXPENSE TABLE
            .append("<h3 style='margin-bottom:10px;'>🧾 Expense Details</h3>");

        if (expenses.isEmpty()) {

            html.append("<p>No expenses found</p>");

        } else {

            html.append("<table style='width:100%;border-collapse:collapse;font-size:14px;'>")
                .append("<tr style='background:#f1f5f9;'>")
                .append("<th style='padding:10px;border:1px solid #ddd;'>Payer</th>")
                .append("<th style='padding:10px;border:1px solid #ddd;'>Amount</th>")
                .append("<th style='padding:10px;border:1px solid #ddd;'>Category</th>")
                .append("<th style='padding:10px;border:1px solid #ddd;'>Description</th>")
                .append("</tr>");

            for (Expense e : expenses) {

                User payer = userRepository.findById(e.getPaidBy()).orElse(null);

                html.append("<tr>")
                    .append("<td style='padding:8px;border:1px solid #ddd;'>")
                    .append(payer != null ? payer.getName() : "Unknown")
                    .append("</td>")
                    .append("<td style='padding:8px;border:1px solid #ddd;'>₹")
                    .append(e.getAmount())
                    .append("</td>")
                    .append("<td style='padding:8px;border:1px solid #ddd;'>")
                    .append(e.getCategory() != null ? e.getCategory() : "-")
                    .append("</td>")
                    .append("<td style='padding:8px;border:1px solid #ddd;'>")
                    .append(e.getDescription())
                    .append("</td>")
                    .append("</tr>");
            }

            html.append("</table>");
        }

        html.append("<hr style='border:none;border-top:1px solid #eee;margin:20px 0;'>")

            // 🔥 SETTLEMENT
            .append("<h3>💰 Settlement Summary</h3>");

        if (settlements.isEmpty()) {

            html.append("<p>No dues remaining ✅</p>");

        } else {

            for (SettlementDTO s : settlements) {

                User toUser = userRepository.findById(s.getToUserId()).orElse(null);
                if (toUser == null) continue;

                String upiId = toUser.getUpiId();

                if (upiId == null || upiId.isBlank()) continue;

                String upiLink = "upi://pay?pa=" + upiId +
                        "&pn=" + toUser.getName() +
                        "&am=" + s.getAmount() +
                        "&cu=INR";

                String cid = "qr_" + s.getToUserId();

                html.append("<div style='margin-top:20px;padding:15px;background:#f9fafb;"
                        + "border-radius:10px;border:1px solid #eee;'>")

                    // QR
                    .append("<img src='cid:").append(cid)
                    .append("' width='140' style='display:block;margin:auto;'/>")

                    // TEXT
                    .append("<p style='text-align:center;font-size:15px;margin-top:10px;'>")
                    .append("<b>").append(s.getFromUser()).append("</b> pays <b>")
                    .append(s.getToUser()).append("</b> ₹")
                    .append(s.getAmount())
                    .append("</p>")

                    // BUTTON
                    .append("<div style='text-align:center;margin-top:10px;'>")
                    .append("<a href='").append(upiLink)
                    .append("' style='background:#16a34a;color:white;"
                            + "padding:10px 18px;border-radius:8px;text-decoration:none;font-size:14px;'>")
                    .append("Pay via UPI")
                    .append("</a>")
                    .append("</div>")

                    // UPI TEXT
                    .append("<p style='text-align:center;font-size:12px;color:#555;margin-top:8px;'>")
                    .append("UPI: ").append(upiId)
                    .append("</p>")

                    .append("</div>");
            }
        }

        html.append("</div>")

            // FOOTER
            .append("<div style='text-align:center;padding:15px;font-size:12px;color:#777;'>")
            .append("Thanks for using SplitMates ❤️")
            .append("</div>")

            .append("</div></body></html>");

        return html.toString();
    }

    public void addExpense(Long tripId, Double amount) {

        Trip trip = getTripById(tripId);

        Double current = trip.getTotalExpense() == null ? 0.0 : trip.getTotalExpense();
        trip.setTotalExpense(current + amount);

        tripRepo.save(trip);
    }
    
    public void deleteTrip(Long tripId) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        tripRepo.delete(trip);
    }
    
 // 🔥 NEW METHOD → CALL AFTER RAZORPAY PAYMENT SUCCESS
    public void sendPaymentEmail(Long tripId, Long fromUserId, Long toUserId, Double amount) {
    	System.out.println("🔥 Sending email...");
        List<TripMember> members = memberRepo.findByTripId(tripId);

        User payer = userRepository.findById(fromUserId).orElse(null);
        User receiver = userRepository.findById(toUserId).orElse(null);

        if (payer == null || receiver == null) return;

        String subject = "💰 Payment Done in Trip";

        for (TripMember m : members) {

            User user = userRepository.findById(m.getUserId()).orElse(null);
            if (user == null) continue;

            String body = "<!DOCTYPE html>"
            		+ "<html>"
            		+ "<body style='margin:0;padding:0;background:#f4f6f8;font-family:Arial;'>"

            		+ "<div style='max-width:600px;margin:30px auto;background:#ffffff;"
            		+ "border-radius:12px;overflow:hidden;box-shadow:0 10px 25px rgba(0,0,0,0.1);'>"

            		+ "<div style='background:#4f46e5;padding:20px;text-align:center;color:white;'>"
            		+ "<h2 style='margin:0;'>SplitMates 💰</h2>"
            		+ "<p style='margin:0;font-size:14px;'>Payment Update</p>"
            		+ "</div>"

            		+ "<div style='padding:25px;'>"

            		+ "<h3 style='margin-top:0;color:#111;'>Hello " + user.getName() + ",</h3>"

            		+ "<p style='font-size:15px;color:#444;'>"
            		+ "<b>" + payer.getName() + "</b> has paid "
            		+ "<span style='color:#16a34a;font-weight:bold;'>₹" + amount + "</span> "
            		+ "to <b>" + receiver.getName() + "</b>."
            		+ "</p>"

            		+ "<div style='background:#f1f5f9;padding:15px;border-radius:10px;margin:20px 0;'>"
            		+ "<p style='margin:0;font-size:14px;'>"
            		+ "✅ Your trip balances have been updated successfully."
            		+ "</p>"
            		+ "</div>"

            		+ "<div style='text-align:center;margin-top:20px;'>"
            		+ "<a href='#' style='background:#4f46e5;color:white;padding:12px 20px;"
            		+ "text-decoration:none;border-radius:8px;font-size:14px;'>"
            		+ "View Trip</a>"
            		+ "</div>"

            		+ "</div>"

            		+ "<div style='text-align:center;padding:15px;font-size:12px;color:#888;'>"
            		+ "© 2026 SplitMates • All rights reserved"
            		+ "</div>"

            		+ "</div>"

            		+ "</body></html>";

            emailService.sendTripSummary(user.getEmail(), subject, body, new HashMap<>());
        }
    }
}