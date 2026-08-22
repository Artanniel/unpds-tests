package com.artantech.paymentservice.validator.ddt;

import com.artantech.paymentservice.exceptions.DailyLimitExceededException;
import com.artantech.paymentservice.exceptions.PaymentLimitException;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.repository.PaymentRepository;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyLimitValidatorDataDrivenTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DailyLimitValidator dailyLimitValidator;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(strings = { "0.00", "-0.01", "-100.00", "-9999.99" })
    @DisplayName("DDT @ValueSource: Deve rejeitar montantes menores ou iguais a zero")
    void shouldRejectZeroOrNegativeAmounts(String amountString) {
        BigDecimal invalidAmount = new BigDecimal(amountString);

        assertThatThrownBy(() -> dailyLimitValidator.validateDailyLimit(PaymentSource.PIX, invalidAmount))
                .isInstanceOf(PaymentLimitException.class)
                .hasMessage("Payment amount must be greater than zero");
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "1000.00, 999.99, true",
        "1500.00, 500.00, true",
        "1500.00, 500.01, false",
        "0.00, 2000.00, true",
        "0.00, 2000.01, false"
    })
    @DisplayName("DDT @CsvSource: Validação de montante acumulado versus limite diário")
    void shouldValidateDailyLimitWithCsvData(String currentTotalStr, String newAmountStr, boolean shouldPass) {
        BigDecimal currentTotal = new BigDecimal(currentTotalStr);
        BigDecimal newAmount = new BigDecimal(newAmountStr);

        when(paymentRepository.sumAmountByPaymentSourceAndDateRange(eq(PaymentSource.PIX), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(currentTotal);

        if (shouldPass) {
            assertThatCode(() -> dailyLimitValidator.validateDailyLimit(PaymentSource.PIX, newAmount))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> dailyLimitValidator.validateDailyLimit(PaymentSource.PIX, newAmount))
                    .isInstanceOf(DailyLimitExceededException.class);
        }
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideNullOrEmptyCases")
    @DisplayName("DDT @MethodSource: Deve ignorar validação se a fonte ou valor forem nulos")
    void shouldIgnoreValidationWhenSourceOrAmountIsNull(PaymentSource source, BigDecimal amount) {
        assertThatCode(() -> dailyLimitValidator.validateDailyLimit(source, amount))
                .doesNotThrowAnyException();
    }

    static Stream<Arguments> provideNullOrEmptyCases() {
        return Stream.of(
            arguments(null, new BigDecimal("100.00")),
            arguments(PaymentSource.CREDIT_CARD, null),
            arguments(null, null)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(DailyLimitScenarioArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação em diferentes modalidades de pagamento (PIX, CREDIT_CARD, DEBIT_CARD)")
    void shouldValidateScenariosPerPaymentSource(PaymentSource source, BigDecimal currentTotal, BigDecimal newAmount, boolean isValid) {
        when(paymentRepository.sumAmountByPaymentSourceAndDateRange(eq(source), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(currentTotal);

        if (isValid) {
            assertThatCode(() -> dailyLimitValidator.validateDailyLimit(source, newAmount))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> dailyLimitValidator.validateDailyLimit(source, newAmount))
                    .isInstanceOf(DailyLimitExceededException.class);
        }
    }

    static class DailyLimitScenarioArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments(PaymentSource.PIX, new BigDecimal("100.00"), new BigDecimal("500.00"), true),
                arguments(PaymentSource.CREDIT_CARD, new BigDecimal("1900.00"), new BigDecimal("200.00"), false),
                arguments(PaymentSource.DEBIT_CARD, null, new BigDecimal("1500.00"), true) // null na soma do banco (nenhum pagamento anterior)
            );
        }
    }
}
