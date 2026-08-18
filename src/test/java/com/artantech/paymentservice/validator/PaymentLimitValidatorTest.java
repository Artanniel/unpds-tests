package com.artantech.paymentservice.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.artantech.paymentservice.exceptions.DailyLimitExceededException;
import com.artantech.paymentservice.exceptions.PaymentLimitException;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.repository.PaymentRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentLimitValidatorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DailyLimitValidator validator;

    @Test
    @DisplayName("a. Valor (amount) acima do limite diário de R$ 2000")
    void shouldThrowExceptionWhenAmountExceedsDailyLimitOf2000() {
        // "Mocka" o comportamento utilizando o Argument Matcher any(), retornando R$ 1800.00
        when(paymentRepository.sumAmountByPaymentSourceAndDateRange(any(), any(), any()))
                .thenReturn(new BigDecimal("1800.00"));

        assertThatThrownBy(() -> validator.validateDailyLimit(PaymentSource.PIX, new BigDecimal("200.01")))
                .isInstanceOf(DailyLimitExceededException.class)
                .hasMessageContaining("Daily limit of 2000.00 exceeded");
    }

    @Test
    @DisplayName("b. Valor (amount) como R$ 0")
    void shouldThrowExceptionWhenAmountIsZero() {
        assertThatThrownBy(() -> validator.validateDailyLimit(PaymentSource.PIX, BigDecimal.ZERO))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessage("Payment amount must be greater than zero");
    }

    @Test
    @DisplayName("c. Valor (amount) como R$ -5")
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThatThrownBy(() -> validator.validateDailyLimit(PaymentSource.PIX, new BigDecimal("-5.00")))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessage("Payment amount must be greater than zero");
    }
}
