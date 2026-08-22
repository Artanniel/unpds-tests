package com.artantech.paymentservice.exceptions.ddt;

import com.artantech.paymentservice.dto.ErrorResponseDTO;
import com.artantech.paymentservice.exceptions.DailyLimitExceededException;
import com.artantech.paymentservice.exceptions.GlobalExceptionHandler;
import com.artantech.paymentservice.exceptions.InvalidStatusTransitionException;
import com.artantech.paymentservice.exceptions.PaymentLimitException;
import com.artantech.paymentservice.exceptions.PaymentNotFoundException;
import com.artantech.paymentservice.model.PaymentSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class GlobalExceptionHandlerDataDrivenTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(longs = { 1L, 42L, 999L, 10500L })
    @DisplayName("DDT @ValueSource: Deve formatar PaymentNotFoundException corretamente para variados IDs")
    void shouldHandlePaymentNotFoundExceptionWithVariousIds(long id) {
        PaymentNotFoundException ex = new PaymentNotFoundException(id);

        ResponseEntity<ErrorResponseDTO> response = handler.handlePaymentNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Payment not found with ID: " + id);
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "Limit exceeded for PIX, 422, Unprocessable Entity",
        "Daily limit of 2000.00 exceeded, 422, Unprocessable Entity",
        "Invalid amount specified, 422, Unprocessable Entity"
    })
    @DisplayName("DDT @CsvSource: Deve tratar PaymentLimitException retornando HTTP 422")
    void shouldHandlePaymentLimitExceptionWithCsvData(String message, int expectedStatus, String expectedTitle) {
        PaymentLimitException ex = new PaymentLimitException(message);

        ResponseEntity<ErrorResponseDTO> response = handler.handlePaymentLimit(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo(expectedTitle);
        assertThat(response.getBody().message()).isEqualTo(message);
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideExceptionScenarios")
    @DisplayName("DDT @MethodSource: Validação de manipuladores de erro genéricos e de leitura HTTP")
    void shouldHandleGenericAndNotReadableExceptions(Exception ex, HttpStatus expectedStatus, String expectedMessage) {
        ResponseEntity<ErrorResponseDTO> response;

        if (ex instanceof HttpMessageNotReadableException notReadableEx) {
            response = handler.handleHttpMessageNotReadable(notReadableEx);
        } else if (ex instanceof InvalidStatusTransitionException transitionEx) {
            response = handler.handleInvalidStatusTransition(transitionEx);
        } else {
            throw new IllegalArgumentException("Unsupported exception type for test");
        }

        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
    }

    static Stream<Arguments> provideExceptionScenarios() {
        return Stream.of(
            arguments(new HttpMessageNotReadableException("Malformed JSON", (org.springframework.http.HttpInputMessage) null), HttpStatus.BAD_REQUEST, "Invalid request payload or malformed JSON"),
            arguments(new InvalidStatusTransitionException("Cannot change status from PAID back to PENDING"), HttpStatus.CONFLICT, "Cannot change status from PAID back to PENDING")
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(DailyLimitExceptionArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação de mensagem de DailyLimitExceededException")
    void shouldValidateDailyLimitExceededExceptionFormatting(DailyLimitExceededException ex, String expectedMessageSubstring) {
        ResponseEntity<ErrorResponseDTO> response = handler.handlePaymentLimit(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains(expectedMessageSubstring);
    }

    static class DailyLimitExceptionArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            DailyLimitExceededException ex1 = new DailyLimitExceededException(
                    PaymentSource.PIX, new BigDecimal("2000.00"), new BigDecimal("1800.00"), new BigDecimal("300.00"));
            DailyLimitExceededException ex2 = new DailyLimitExceededException(
                    PaymentSource.CREDIT_CARD, new BigDecimal("2000.00"), new BigDecimal("1500.00"), new BigDecimal("600.00"));

            return Stream.of(
                arguments(ex1, "PIX"),
                arguments(ex2, "CREDIT_CARD")
            );
        }
    }
}
