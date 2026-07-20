package com.jhorgi.payment_service.payment.controller;

import com.jhorgi.payment_service.payment.dto.CreatePaymentRequest;
import com.jhorgi.payment_service.payment.dto.PaymentResponse;
import com.jhorgi.payment_service.payment.entity.Payment;
import com.jhorgi.payment_service.payment.service.PaymentService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable UUID orderId,
                                                        HttpServletRequest httpRequest) {
        String customerId = extractCustomerId(httpRequest);
        Payment payment = paymentService.getByOrderId(orderId, customerId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @PostMapping("/{paymentId}/pay")
    public ResponseEntity<PaymentResponse> pay(@PathVariable UUID paymentId,
                                               HttpServletRequest httpRequest) {
        String customerId = extractCustomerId(httpRequest);
        Payment payment = paymentService.pay(paymentId, customerId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> fail(@PathVariable UUID paymentId,
                                                HttpServletRequest httpRequest) {
        String customerId = extractCustomerId(httpRequest);
        Payment payment = paymentService.fail(paymentId, customerId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    private String extractCustomerId(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("jwtClaims");
        return claims.getSubject();
    }
}
