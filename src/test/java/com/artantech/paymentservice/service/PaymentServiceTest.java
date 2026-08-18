package com.artantech.paymentservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.dto.PaymentStatusUpdateDTO;
import com.artantech.paymentservice.exceptions.DailyLimitExceededException;
import com.artantech.paymentservice.exceptions.PaymentNotFoundException;
import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;
import com.artantech.paymentservice.repository.PaymentRepository;
import com.artantech.paymentservice.service.impl.PaymentServiceImpl;
import com.artantech.paymentservice.validator.DailyLimitValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DailyLimitValidator dailyLimitValidator;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Should save a payment when limit is not exceeded")
    void shouldSavePaymentWhenLimitIsNotExceeded() {
        when(paymentRepository.save(any()))
                .thenAnswer(invocationOnMock -> {
                    Payment p = invocationOnMock.getArgument(0);
                    return new Payment(1L, p.getTransactionId(), p.getPaymentSource(), p.getAmount(),
                            p.getStatus(), p.getPayerId(), p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now());
                });

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("TX-100", PaymentSource.PIX, new BigDecimal("100.50"), "PAYER-1");

        PaymentResponseDTO createdPayment = paymentService.createPayment(paymentRequest);

        assertThat(createdPayment.id()).isNotNull();
        assertThat(createdPayment.payerId()).isEqualTo(paymentRequest.payerId());
        assertThat(createdPayment.paymentSource()).isEqualTo(PaymentSource.PIX);
        assertThat(createdPayment.amount()).isEqualByComparingTo(paymentRequest.amount());
        assertThat(createdPayment.status()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("Não deve criar pagamento se a validação de limite diário falhar")
    void shouldNotCreatePaymentWhenDailyLimitExceeded() {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO("TX-101", PaymentSource.PIX, new BigDecimal("2500.00"),
                "PAYER-1");

        doThrow(new DailyLimitExceededException(PaymentSource.PIX, new BigDecimal("2000.00"), BigDecimal.ZERO,
                new BigDecimal("2500.00")))
                .when(dailyLimitValidator).validateDailyLimit(PaymentSource.PIX, new BigDecimal("2500.00"));

        assertThatThrownBy(() -> paymentService.createPayment(requestDTO))
                .isInstanceOf(DailyLimitExceededException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Deve retornar pagamento por ID quando existir")
    void shouldGetPaymentByIdWhenExists() {
        Payment payment = new Payment(1L, "TX-100", PaymentSource.PIX, new BigDecimal("250.00"), PaymentStatus.PENDING,
                "PAYER-1", LocalDateTime.now());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponseDTO responseDTO = paymentService.getPaymentById(1L);

        assertThat(responseDTO.id()).isEqualTo(1L);
        assertThat(responseDTO.transactionId()).isEqualTo("TX-100");
    }

    @Test
    @DisplayName("Deve lançar PaymentNotFoundException ao buscar ID inexistente")
    void shouldThrowExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(99L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessage("Payment not found with ID: 99");
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao buscar todos os pagamentos de um banco vazio")
    void shouldReturnEmptyListWhenNoPaymentsExist() {
        when(paymentRepository.findAllByOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

        List<PaymentResponseDTO> payments = paymentService.getAllPayments();

        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao buscar pagamentos de um pagador sem transações")
    void shouldReturnEmptyListWhenNoPaymentsForPayer() {
        when(paymentRepository.findByPayerIdOrderByCreatedAtAsc("PAYER-UNKNOWN")).thenReturn(Collections.emptyList());

        List<PaymentResponseDTO> payments = paymentService.getPaymentsByPayerId("PAYER-UNKNOWN");

        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar status do pagamento para PAID")
    void shouldUpdatePaymentStatusToPaid() {
        Payment payment = new Payment(1L, "TX-100", PaymentSource.PIX, new BigDecimal("250.00"), PaymentStatus.PENDING,
                "PAYER-1", LocalDateTime.now());
        Payment updatedPayment = new Payment(1L, "TX-100", PaymentSource.PIX, new BigDecimal("250.00"),
                PaymentStatus.PAID, "PAYER-1", payment.getCreatedAt());

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        PaymentResponseDTO responseDTO = paymentService.updatePaymentStatus(1L,
                new PaymentStatusUpdateDTO(PaymentStatus.PAID));

        assertThat(responseDTO.status()).isEqualTo(PaymentStatus.PAID);
    }
}
