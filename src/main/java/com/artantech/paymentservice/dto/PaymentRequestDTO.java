package com.artantech.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import com.artantech.paymentservice.model.PaymentSource;

public record PaymentRequestDTO(
                String transactionId,

                @NotNull(message = "Payment source is required") PaymentSource paymentSource,

                @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,

                @NotBlank(message = "Payer ID is required") String payerId) {
}
