package com.artantech.paymentservice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.artantech.paymentservice.exceptions.InvalidStatusTransitionException;
import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentStatusTransitionTest {

    @Test
    @DisplayName("Deve inicializar o pagamento com status PENDING por padrão")
    void shouldInitializePaymentWithPendingStatus() {
        Payment payment = new Payment("TX-001", PaymentSource.PIX, new BigDecimal("100.00"), "PAYER-1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getCreatedAt()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = { "PAID", "FRAUD" })
    @DisplayName("Deve permitir alterar status de PENDING para PAID ou FRAUD")
    void shouldAllowTransitionFromPendingToPaidOrFraud(PaymentStatus targetStatus) {
        Payment payment = new Payment("TX-001", PaymentSource.PIX, new BigDecimal("100.00"), "PAYER-1");

        payment.updateStatus(targetStatus);

        assertThat(payment.getStatus()).isEqualTo(targetStatus);
    }

    @Test
    @DisplayName("Não deve permitir transição de PAID para PENDING")
    void shouldNotAllowTransitionFromPaidToPending() {
        Payment payment = new Payment("TX-001", PaymentSource.PIX, new BigDecimal("100.00"), "PAYER-1");
        payment.updateStatus(PaymentStatus.PAID);

        assertThatThrownBy(() -> payment.updateStatus(PaymentStatus.PENDING))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Cannot change status from PAID back to PENDING");
    }

    @Test
    @DisplayName("Não deve permitir transição de FRAUD para PENDING")
    void shouldNotAllowTransitionFromFraudToPending() {
        Payment payment = new Payment("TX-001", PaymentSource.CREDIT_CARD, new BigDecimal("250.00"), "PAYER-1");
        payment.updateStatus(PaymentStatus.FRAUD);

        assertThatThrownBy(() -> payment.updateStatus(PaymentStatus.PENDING))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Cannot change status from FRAUD back to PENDING");
    }

    @Test
    @DisplayName("Deve lançar exceção se status fornecido for nulo")
    void shouldThrowExceptionWhenStatusIsNull() {
        Payment payment = new Payment("TX-001", PaymentSource.DEBIT_CARD, new BigDecimal("50.00"), "PAYER-1");

        assertThatThrownBy(() -> payment.updateStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status cannot be null");
    }
}
