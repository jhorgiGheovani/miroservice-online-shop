package com.jhorgi.payment_service.payment.repository;

import com.jhorgi.payment_service.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
