package com.jhorgi.payment_service.payment.service;

import com.jhorgi.payment_service.kafka.PaymentEventProducer;
import com.jhorgi.payment_service.payment.dto.CreatePaymentRequest;
import com.jhorgi.payment_service.payment.entity.Payment;
import com.jhorgi.payment_service.payment.entity.PaymentStatus;
import com.jhorgi.payment_service.payment.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer eventProducer;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventProducer eventProducer) {
        this.paymentRepository = paymentRepository;
        this.eventProducer = eventProducer;
    }

    public Payment create(CreatePaymentRequest request) {
        Payment payment = new Payment(
                request.getOrderId(),
                request.getCustomerId(),
                request.getEmail(),
                request.getAmount(),
                PaymentStatus.PENDING,
                LocalDateTime.now()
        );
        return paymentRepository.save(payment);
    }

    public Payment getById(UUID paymentId, String requestingCustomerId) {
        Payment payment = findOrThrow(paymentId);
        checkOwnership(payment, requestingCustomerId);
        return payment;
    }

    public Payment pay(UUID paymentId, String requestingCustomerId) {
        Payment payment = findOrThrow(paymentId);
        checkOwnership(payment, requestingCustomerId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment is not in PENDING state");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        Payment saved = paymentRepository.save(payment);
        eventProducer.publish(saved.getOrderId(), "SUCCESS", saved.getEmail(), saved.getAmount());
        return saved;
    }

    public Payment fail(UUID paymentId, String requestingCustomerId) {
        Payment payment = findOrThrow(paymentId);
        checkOwnership(payment, requestingCustomerId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment is not in PENDING state");
        }

        payment.setStatus(PaymentStatus.FAILED);
        Payment saved = paymentRepository.save(payment);
        eventProducer.publish(saved.getOrderId(), "FAILED", saved.getEmail(), saved.getAmount());
        return saved;
    }

    private Payment findOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private void checkOwnership(Payment payment, String customerId) {
        if (!payment.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }
}
