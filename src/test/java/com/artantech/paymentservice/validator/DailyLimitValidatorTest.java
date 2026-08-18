package com.artantech.paymentservice.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.artantech.paymentservice.exceptions.DailyLimitExceededException;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.repository.PaymentRepository;
import com.artantech.paymentservice.validator.DailyLimitValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyLimitValidatorTest {

        @Mock
        private PaymentRepository paymentRepository;

        @InjectMocks
        private DailyLimitValidator dailyLimitValidator;

        @Test
        @DisplayName("Deve permitir pagamento quando a soma com o acumulado for exatamente 2000.00")
        void shouldAllowPaymentWhenTotalReachesExactlyLimit() {
                when(paymentRepository.sumAmountByPaymentSourceAndDateRange(eq(PaymentSource.PIX),
                                any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(new BigDecimal("1500.00"));

                assertThatCode(() -> dailyLimitValidator.validateDailyLimit(PaymentSource.PIX,
                                new BigDecimal("500.00")))
                                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve rejeitar pagamento quando a soma ultrapassar 2000.00 por apenas 0.01 centavo")
        void shouldThrowExceptionWhenTotalExceedsLimit() {
                when(paymentRepository.sumAmountByPaymentSourceAndDateRange(eq(PaymentSource.PIX),
                                any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(new BigDecimal("1800.00"));

                assertThatThrownBy(() -> dailyLimitValidator.validateDailyLimit(PaymentSource.PIX,
                                new BigDecimal("200.01")))
                                .isInstanceOf(DailyLimitExceededException.class)
                                .hasMessageContaining("Daily limit of 2000.00 exceeded for payment source PIX");
        }

        @Test
        @DisplayName("Deve permitir pagamento quando não houver registros prévios no dia (acumulado 0)")
        void shouldAllowPaymentWhenNoPreviousPaymentsInDay() {
                when(paymentRepository.sumAmountByPaymentSourceAndDateRange(eq(PaymentSource.CREDIT_CARD),
                                any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(BigDecimal.ZERO);

                assertThatCode(() -> dailyLimitValidator.validateDailyLimit(PaymentSource.CREDIT_CARD,
                                new BigDecimal("2000.00")))
                                .doesNotThrowAnyException();
        }
}
