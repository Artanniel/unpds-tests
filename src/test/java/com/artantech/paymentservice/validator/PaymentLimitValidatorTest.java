package com.artantech.paymentservice.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.artantech.paymentservice.exceptions.DailyLimitExceededException;
import com.artantech.paymentservice.exceptions.PaymentLimitException;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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

    // --- Exercício: casos-limite (edge cases) via factory method + @MethodSource ---

    static Stream<BigDecimal> edgeCasesForLimit() {
        return Stream.of(
                new BigDecimal("00.0"),
                new BigDecimal("2000.01"),
                new BigDecimal("3500.00")
        );
    }

    @ParameterizedTest
    @MethodSource("edgeCasesForLimit")
    @DisplayName("edge: valores limítrofes devem violar o limite diário de R$ 2000,00")
    void edge(BigDecimal amount) {
        assertThat(validator.isLimitExceeded(PaymentSource.PIX, amount)).isTrue();
    }

    // --- Exercício: happy paths (valores válidos) via factory method + @MethodSource ---

    static Stream<BigDecimal> happyPathsForLimit() {
        return Stream.of(
                new BigDecimal("00.1"),
                new BigDecimal("1999.99"),
                new BigDecimal("2000.00")
        );
    }

    @ParameterizedTest
    @MethodSource("happyPathsForLimit")
    @DisplayName("happyPaths: valores válidos não devem violar o limite diário de R$ 2000,00")
    void happyPaths(BigDecimal amount) {
        assertThat(validator.isLimitExceeded(PaymentSource.PIX, amount)).isFalse();
    }
}
