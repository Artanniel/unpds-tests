package com.artantech.paymentservice.repository.ddt;

import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;
import com.artantech.paymentservice.repository.PaymentRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb_ddt;DB_CLOSE_DELAY=-1;MODE=LEGACY"
})
class PaymentRepositoryDataDrivenTest {

    @Autowired
    private PaymentRepository paymentRepository;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(strings = { "PIX", "CREDIT_CARD", "DEBIT_CARD" })
    @DisplayName("DDT @ValueSource: Consulta de soma zerada para fontes sem pagamentos cadastrados")
    void shouldReturnZeroForSourcesWithoutPayments(String sourceName) {
        PaymentSource source = PaymentSource.valueOf(sourceName);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal sum = paymentRepository.sumAmountByPaymentSourceAndDateRange(source, startOfDay, endOfDay);

        assertThat(sum).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "PIX, 150.00, 250.00, 400.00",
        "CREDIT_CARD, 500.00, 300.00, 800.00",
        "DEBIT_CARD, 75.50, 24.50, 100.00"
    })
    @DisplayName("DDT @CsvSource: Validação do cálculo de soma total por fonte no intervalo do dia")
    void shouldCalculateDailySumPerSource(PaymentSource source, String val1, String val2, String expectedSum) {
        LocalDateTime now = LocalDateTime.now();
        paymentRepository.save(new Payment(null, "TX-A", source, new BigDecimal(val1), PaymentStatus.PAID, "PAYER-1", now));
        paymentRepository.save(new Payment(null, "TX-B", source, new BigDecimal(val2), PaymentStatus.PAID, "PAYER-1", now));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal actualSum = paymentRepository.sumAmountByPaymentSourceAndDateRange(source, startOfDay, endOfDay);

        assertThat(actualSum).isEqualByComparingTo(new BigDecimal(expectedSum));
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("providePayerIdsToSearch")
    @DisplayName("DDT @MethodSource: Busca de pagamentos por payerId com diferentes volumes")
    void shouldFindPaymentsByPayerId(String payerId, int expectedCount) {
        LocalDateTime now = LocalDateTime.now();
        paymentRepository.save(new Payment(null, "TX-1", PaymentSource.PIX, new BigDecimal("100.00"), PaymentStatus.PENDING, "CLIENT-A", now));
        paymentRepository.save(new Payment(null, "TX-2", PaymentSource.CREDIT_CARD, new BigDecimal("200.00"), PaymentStatus.PENDING, "CLIENT-A", now));
        paymentRepository.save(new Payment(null, "TX-3", PaymentSource.DEBIT_CARD, new BigDecimal("300.00"), PaymentStatus.PENDING, "CLIENT-B", now));

        List<Payment> results = paymentRepository.findByPayerIdOrderByCreatedAtAsc(payerId);

        assertThat(results).hasSize(expectedCount);
    }

    static Stream<Arguments> providePayerIdsToSearch() {
        return Stream.of(
            arguments("CLIENT-A", 2),
            arguments("CLIENT-B", 1),
            arguments("CLIENT-NON-EXISTENT", 0)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(PaymentEntityBatchArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação da ordenação padrão por data de criação")
    void shouldMaintainOrderOfPaymentsByCreatedAt(List<Payment> paymentsToSave, String expectedFirstTxId) {
        paymentRepository.saveAll(paymentsToSave);

        List<Payment> sortedPayments = paymentRepository.findAllByOrderByCreatedAtAsc();

        assertThat(sortedPayments).isNotEmpty();
        assertThat(sortedPayments.get(0).getTransactionId()).isEqualTo(expectedFirstTxId);
    }

    static class PaymentEntityBatchArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            LocalDateTime now = LocalDateTime.now();
            Payment older = new Payment(null, "TX-OLDER", PaymentSource.PIX, new BigDecimal("10.00"), PaymentStatus.PAID, "P1", now.minusHours(5));
            Payment newer = new Payment(null, "TX-NEWER", PaymentSource.PIX, new BigDecimal("20.00"), PaymentStatus.PAID, "P1", now);

            return Stream.of(
                arguments(List.of(newer, older), "TX-OLDER"),
                arguments(List.of(older, newer), "TX-OLDER")
            );
        }
    }
}
