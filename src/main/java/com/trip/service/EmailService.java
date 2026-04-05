package com.trip.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTripSummary(String to, String subject, String html, Map<String, byte[]> qrImages) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            // 🔥 add multiple QR images
            for (Map.Entry<String, byte[]> entry : qrImages.entrySet()) {
                helper.addInline(entry.getKey(),
                        new ByteArrayResource(entry.getValue()),
                        "image/png");
            }

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email send failed");
        }
    
    }
    
    public void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            System.out.println("✅ Email sent to: " + to);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Email failed");
        }
    }
    
}