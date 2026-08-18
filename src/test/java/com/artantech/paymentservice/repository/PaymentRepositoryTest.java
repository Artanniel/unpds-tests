package com.artantech.paymentservice.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;
import com.artantech.paymentservice.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=LEGACY"
})
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve somar corretamente os valores de pagamentos criados no dia corrente por fonte de pagamento")
    void shouldSumAmountByPaymentSourceAndDateRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Payment p1 = new Payment(null, "TX-1", PaymentSource.PIX, new BigDecimal("500.00"), PaymentStatus.PENDING,
                "PAYER-1", now.minusHours(2));
        Payment p2 = new Payment(null, "TX-2", PaymentSource.PIX, new BigDecimal("300.00"), PaymentStatus.PENDING,
                "PAYER-2", now.minusHours(1));
        Payment pOtherSource = new Payment(null, "TX-3", PaymentSource.CREDIT_CARD, new BigDecimal("700.00"),
                PaymentStatus.PENDING, "PAYER-1", now);
        Payment pYesterday = new Payment(null, "TX-4", PaymentSource.PIX, new BigDecimal("1000.00"),
                PaymentStatus.PENDING, "PAYER-1", now.minusDays(1));

        paymentRepository.saveAll(List.of(p1, p2, pOtherSource, pYesterday));

        BigDecimal totalPixToday = paymentRepository.sumAmountByPaymentSourceAndDateRange(PaymentSource.PIX, startOfDay,
                endOfDay);
        BigDecimal totalCardToday = paymentRepository.sumAmountByPaymentSourceAndDateRange(PaymentSource.CREDIT_CARD,
                startOfDay, endOfDay);

        assertThat(totalPixToday).isEqualByComparingTo("800.00");
        assertThat(totalCardToday).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("Deve retornar 0 ao somar fonte sem pagamentos no dia")
    void shouldReturnZeroWhenNoPaymentsForSourceInDateRange() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal total = paymentRepository.sumAmountByPaymentSourceAndDateRange(PaymentSource.DEBIT_CARD, startOfDay,
                endOfDay);

        assertThat(total).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Deve buscar todos os pagamentos ordenados por data de criação")
    void shouldFindAllOrderedByCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        Payment p1 = new Payment(null, "TX-1", PaymentSource.PIX, new BigDecimal("100.00"), PaymentStatus.PENDING,
                "PAYER-1", now.minusMinutes(10));
        Payment p2 = new Payment(null, "TX-2", PaymentSource.PIX, new BigDecimal("200.00"), PaymentStatus.PENDING,
                "PAYER-1", now.minusMinutes(5));

        paymentRepository.saveAll(List.of(p2, p1));

        List<Payment> payments = paymentRepository.findAllByOrderByCreatedAtAsc();

        assertThat(payments).hasSize(2);
        assertThat(payments.get(0).getTransactionId()).isEqualTo("TX-1");
        assertThat(payments.get(1).getTransactionId()).isEqualTo("TX-2");
    }

    @Test
    @DisplayName("Deve buscar pagamentos filtrados por payerId ordenados por data")
    void shouldFindByPayerIdOrderedByCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        Payment p1 = new Payment(null, "TX-1", PaymentSource.PIX, new BigDecimal("100.00"), PaymentStatus.PENDING,
                "PAYER-A", now.minusMinutes(10));
        Payment p2 = new Payment(null, "TX-2", PaymentSource.CREDIT_CARD, new BigDecimal("200.00"),
                PaymentStatus.PENDING, "PAYER-B", now);

        paymentRepository.saveAll(List.of(p1, p2));

        List<Payment> payerAPayments = paymentRepository.findByPayerIdOrderByCreatedAtAsc("PAYER-A");
        List<Payment> payerUnknownPayments = paymentRepository.findByPayerIdOrderByCreatedAtAsc("PAYER-UNKNOWN");

        assertThat(payerAPayments).hasSize(1);
        assertThat(payerAPayments.get(0).getPayerId()).isEqualTo("PAYER-A");
        assertThat(payerUnknownPayments).isEmpty();
    }

    @Test
    @DisplayName("Nao deve somar pagamentos de dias diferentes (utilizando TestEntityManager e clear)")
    void shouldNotSumPaymentsFromDifferentDays() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Payment firstPayment = paymentRepository.save(new Payment(null, "TX-DAY-1", PaymentSource.PIX,
                new BigDecimal("50.00"), PaymentStatus.PENDING, "PAYER-TEST", now));
        Payment secondPayment = paymentRepository.save(new Payment(null, "TX-DAY-2", PaymentSource.PIX,
                new BigDecimal("350.00"), PaymentStatus.PENDING, "PAYER-TEST", now));

        LocalDateTime yesterday = startOfDay.minusDays(1).plusHours(10);

        entityManager.getEntityManager()
                .createQuery("UPDATE Payment payment SET payment.createdAt = :createdAt WHERE payment.id = :id")
                .setParameter("createdAt", yesterday)
                .setParameter("id", secondPayment.getId())
                .executeUpdate();

        entityManager.clear();

        BigDecimal total = paymentRepository.sumAmountByPaymentSourceAndDateRange(PaymentSource.PIX, startOfDay, endOfDay);

        assertThat(total).isEqualByComparingTo(new BigDecimal("50.00"));
    }
}
