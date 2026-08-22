package com.artantech.paymentservice.model.ddt;

import com.artantech.paymentservice.exceptions.InvalidStatusTransitionException;
import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PaymentDataDrivenTest {

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(strings = { "TX-1001", "TX-2002", "TX-3003", "CUSTOM-TX-99" })
    @DisplayName("DDT @ValueSource: Deve instanciar Payment com diferentes IDs de transação válidos")
    void shouldCreatePaymentWithVariousTransactionIds(String transactionId) {
        Payment payment = new Payment(transactionId, PaymentSource.PIX, new BigDecimal("150.00"), "PAYER-01");

        assertThat(payment.getTransactionId()).isEqualTo(transactionId);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "PENDING, PAID, true",
        "PENDING, FRAUD, true",
        "PAID, PENDING, false",
        "FRAUD, PENDING, false",
        "PAID, FRAUD, true"
    })
    @DisplayName("DDT @CsvSource: Validação de matriz de transição de status de pagamento")
    void shouldValidateStatusTransitionMatrix(PaymentStatus currentStatus, PaymentStatus newStatus, boolean shouldSucceed) {
        Payment payment = new Payment(1L, "TX-01", PaymentSource.CREDIT_CARD, new BigDecimal("100.00"), currentStatus, "PAYER-1", LocalDateTime.now());

        if (shouldSucceed) {
            payment.updateStatus(newStatus);
            assertThat(payment.getStatus()).isEqualTo(newStatus);
        } else {
            assertThatThrownBy(() -> payment.updateStatus(newStatus))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessageContaining("Cannot change status from " + currentStatus + " back to PENDING");
        }
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideConstructorCases")
    @DisplayName("DDT @MethodSource: Validação de construtores com valores nulos e defaults")
    void shouldTestConstructorDefaults(Long id, String txId, PaymentSource source, BigDecimal amount, PaymentStatus status, String payerId, LocalDateTime createdAt) {
        Payment payment = new Payment(id, txId, source, amount, status, payerId, createdAt);

        assertThat(payment.getId()).isEqualTo(id);
        assertThat(payment.getTransactionId()).isEqualTo(txId);
        assertThat(payment.getPaymentSource()).isEqualTo(source);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getStatus()).isNotNull();
        assertThat(payment.getCreatedAt()).isNotNull();
    }

    static Stream<Arguments> provideConstructorCases() {
        return Stream.of(
            arguments(1L, "TX-A", PaymentSource.PIX, new BigDecimal("10.00"), null, "PAYER-A", null),
            arguments(2L, "TX-B", PaymentSource.DEBIT_CARD, new BigDecimal("20.00"), PaymentStatus.PAID, "PAYER-B", LocalDateTime.now().minusDays(1)),
            arguments(null, "TX-C", PaymentSource.CREDIT_CARD, new BigDecimal("30.00"), PaymentStatus.FRAUD, "PAYER-C", null)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(PaymentEqualsArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação de comportamento dos métodos equals e hashCode")
    void shouldValidateEqualsAndHashCode(Payment payment1, Object payment2, boolean expectedEquals) {
        assertThat(payment1.equals(payment2)).isEqualTo(expectedEquals);
        if (expectedEquals && payment2 != null) {
            assertThat(payment1.hashCode()).isEqualTo(payment2.hashCode());
        }
    }

    static class PaymentEqualsArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            Payment p1 = new Payment(10L, "TX-SAME", PaymentSource.PIX, new BigDecimal("100.00"), PaymentStatus.PAID, "USER-1", LocalDateTime.now());
            Payment p2Equals = new Payment(10L, "TX-SAME", PaymentSource.CREDIT_CARD, new BigDecimal("200.00"), PaymentStatus.PENDING, "USER-2", LocalDateTime.now());
            Payment p3DifferentId = new Payment(20L, "TX-SAME", PaymentSource.PIX, new BigDecimal("100.00"), PaymentStatus.PAID, "USER-1", LocalDateTime.now());
            Payment p4DifferentTx = new Payment(10L, "TX-DIFF", PaymentSource.PIX, new BigDecimal("100.00"), PaymentStatus.PAID, "USER-1", LocalDateTime.now());

            return Stream.of(
                arguments(p1, p1, true),
                arguments(p1, p2Equals, true),
                arguments(p1, p3DifferentId, false),
                arguments(p1, p4DifferentTx, false),
                arguments(p1, null, false),
                arguments(p1, "NotAPaymentObject", false)
            );
        }
    }
}
