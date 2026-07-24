package com.jhorgi.notif_service.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPaymentNotification(String to, String orderId, String status, BigDecimal amount) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        if ("SUCCESS".equals(status)) {
            msg.setSubject("Payment Successful - Order " + orderId);
            msg.setText("Your payment of Rp " + amount + " for order " + orderId + " was successful. Thank you!");
        } else {
            msg.setSubject("Payment Failed - Order " + orderId);
            msg.setText("Your payment for order " + orderId + " has failed. Please try again or contact support.");
        }
        mailSender.send(msg);
    }
}
