package com.artantech.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;

public record PaymentResponseDTO(
        Long id,
        String transactionId,
        PaymentSource paymentSource,
        BigDecimal amount,
        PaymentStatus status,
        String payerId,
        LocalDateTime createdAt) {
    public static PaymentResponseDTO fromEntity(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getTransactionId(),
                payment.getPaymentSource(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPayerId(),
                payment.getCreatedAt());
    }
}
