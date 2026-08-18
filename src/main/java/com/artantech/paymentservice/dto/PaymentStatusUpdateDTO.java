package com.artantech.paymentservice.dto;

import com.artantech.paymentservice.model.PaymentStatus;

import jakarta.validation.constraints.NotNull;

public record PaymentStatusUpdateDTO(
                @NotNull(message = "Status is required") PaymentStatus status) {
}
