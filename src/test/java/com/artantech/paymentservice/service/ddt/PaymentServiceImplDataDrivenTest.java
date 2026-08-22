package com.artantech.paymentservice.service.ddt;

import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.dto.PaymentStatusUpdateDTO;
import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;
import com.artantech.paymentservice.repository.PaymentRepository;
import com.artantech.paymentservice.service.impl.PaymentServiceImpl;
import com.artantech.paymentservice.validator.DailyLimitValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplDataDrivenTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DailyLimitValidator dailyLimitValidator;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(strings = { "CUSTOM-TX-100", "CUSTOM-TX-200", "MY-UNIQUE-TRANSACTION-ID" })
    @DisplayName("DDT @ValueSource: Deve preservar o transactionId fornecido no PaymentRequestDTO")
    void shouldPreserveCustomTransactionId(String customTxId) {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO(customTxId, PaymentSource.PIX, new BigDecimal("100.00"), "PAYER-01");
        Payment savedPayment = new Payment(1L, customTxId, PaymentSource.PIX, new BigDecimal("100.00"), PaymentStatus.PENDING, "PAYER-01", LocalDateTime.now());

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponseDTO response = paymentService.createPayment(requestDTO);

        assertThat(response.transactionId()).isEqualTo(customTxId);
        verify(dailyLimitValidator).validateDailyLimit(PaymentSource.PIX, new BigDecimal("100.00"));
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "1, PENDING, PAID",
        "2, PENDING, FRAUD",
        "3, PAID, FRAUD"
    })
    @DisplayName("DDT @CsvSource: Deve atualizar status do pagamento corretamente")
    void shouldUpdatePaymentStatusWithCsvData(Long paymentId, PaymentStatus initialStatus, PaymentStatus targetStatus) {
        Payment existingPayment = new Payment(paymentId, "TX-" + paymentId, PaymentSource.CREDIT_CARD, new BigDecimal("250.00"), initialStatus, "PAYER-02", LocalDateTime.now());
        Payment updatedPayment = new Payment(paymentId, "TX-" + paymentId, PaymentSource.CREDIT_CARD, new BigDecimal("250.00"), targetStatus, "PAYER-02", LocalDateTime.now());

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        PaymentResponseDTO response = paymentService.updatePaymentStatus(paymentId, new PaymentStatusUpdateDTO(targetStatus));

        assertThat(response.id()).isEqualTo(paymentId);
        assertThat(response.status()).isEqualTo(targetStatus);
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("providePaymentCreationRequests")
    @DisplayName("DDT @MethodSource: Criação de pagamentos com diferentes origens e montantes")
    void shouldCreatePaymentsFromMethodSource(PaymentRequestDTO requestDTO, PaymentSource expectedSource, BigDecimal expectedAmount) {
        Payment savedPayment = new Payment(10L, "TX-GEN-123", expectedSource, expectedAmount, PaymentStatus.PENDING, requestDTO.payerId(), LocalDateTime.now());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponseDTO response = paymentService.createPayment(requestDTO);

        assertThat(response.paymentSource()).isEqualTo(expectedSource);
        assertThat(response.amount()).isEqualByComparingTo(expectedAmount);
    }

    static Stream<Arguments> providePaymentCreationRequests() {
        return Stream.of(
            arguments(new PaymentRequestDTO(null, PaymentSource.PIX, new BigDecimal("50.00"), "USER-A"), PaymentSource.PIX, new BigDecimal("50.00")),
            arguments(new PaymentRequestDTO("", PaymentSource.DEBIT_CARD, new BigDecimal("120.50"), "USER-B"), PaymentSource.DEBIT_CARD, new BigDecimal("120.50")),
            arguments(new PaymentRequestDTO("TX-999", PaymentSource.CREDIT_CARD, new BigDecimal("999.00"), "USER-C"), PaymentSource.CREDIT_CARD, new BigDecimal("999.00"))
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(PayerListArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Consulta de pagamentos por PayerId")
    void shouldGetPaymentsByPayerId(String payerId, List<Payment> mockPayments, int expectedSize) {
        when(paymentRepository.findByPayerIdOrderByCreatedAtAsc(payerId)).thenReturn(mockPayments);

        List<PaymentResponseDTO> results = paymentService.getPaymentsByPayerId(payerId);

        assertThat(results).hasSize(expectedSize);
        if (expectedSize > 0) {
            assertThat(results.get(0).payerId()).isEqualTo(payerId);
        }
    }

    static class PayerListArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            Payment p1 = new Payment(1L, "TX-1", PaymentSource.PIX, new BigDecimal("10.00"), PaymentStatus.PAID, "PAYER-ALPHA", LocalDateTime.now());
            Payment p2 = new Payment(2L, "TX-2", PaymentSource.PIX, new BigDecimal("20.00"), PaymentStatus.PAID, "PAYER-ALPHA", LocalDateTime.now());

            return Stream.of(
                arguments("PAYER-ALPHA", List.of(p1, p2), 2),
                arguments("PAYER-EMPTY", List.of(), 0)
            );
        }
    }
}
