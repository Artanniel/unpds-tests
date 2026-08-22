package com.artantech.paymentservice.controller.ddt;

import com.artantech.paymentservice.controller.PaymentController;
import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.dto.PaymentStatusUpdateDTO;
import com.artantech.paymentservice.exceptions.GlobalExceptionHandler;
import com.artantech.paymentservice.exceptions.PaymentNotFoundException;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;
import com.artantech.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerDataDrivenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(longs = { 900L, 901L, 9999L })
    @DisplayName("DDT @ValueSource: GET /payments/{id} deve retornar HTTP 404 para IDs inexistentes")
    void shouldReturn404ForNonExistentIds(long paymentId) throws Exception {
        when(paymentService.getPaymentById(paymentId)).thenThrow(new PaymentNotFoundException(paymentId));

        mockMvc.perform(get("/payments/" + paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "0.00, Amount must be positive",
        "-10.00, Amount must be positive",
        "-100.50, Amount must be positive"
    })
    @DisplayName("DDT @CsvSource: POST /payments deve rejeitar montantes zerados ou negativos com HTTP 400")
    void shouldRejectInvalidAmountsWith400(String amountStr, String expectedErrorMessage) throws Exception {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO("TX-123", PaymentSource.PIX, new BigDecimal(amountStr), "PAYER-1");

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount", is(expectedErrorMessage)));
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideStatusUpdates")
    @DisplayName("DDT @MethodSource: PUT /payments/{id} deve atualizar status do pagamento")
    void shouldUpdatePaymentStatusFromMethodSource(PaymentStatus targetStatus) throws Exception {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(10L, "TX-10", PaymentSource.CREDIT_CARD, new BigDecimal("100.00"), targetStatus, "PAYER-10", LocalDateTime.now());
        when(paymentService.updatePaymentStatus(eq(10L), any(PaymentStatusUpdateDTO.class))).thenReturn(responseDTO);

        PaymentStatusUpdateDTO updateDTO = new PaymentStatusUpdateDTO(targetStatus);

        mockMvc.perform(put("/payments/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(targetStatus.name())));
    }

    static Stream<Arguments> provideStatusUpdates() {
        return Stream.of(
            arguments(PaymentStatus.PAID),
            arguments(PaymentStatus.FRAUD)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(PaymentRequestArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: POST /payments deve criar pagamento com sucesso para diferentes fontes")
    void shouldCreatePaymentSuccessfullyForMultipleSources(PaymentRequestDTO requestDTO, PaymentSource expectedSource) throws Exception {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(1L, requestDTO.transactionId(), expectedSource, requestDTO.amount(), PaymentStatus.PENDING, requestDTO.payerId(), LocalDateTime.now());
        when(paymentService.createPayment(any(PaymentRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentSource", is(expectedSource.name())));
    }

    static class PaymentRequestArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments(new PaymentRequestDTO("TX-A", PaymentSource.PIX, new BigDecimal("50.00"), "PAYER-A"), PaymentSource.PIX),
                arguments(new PaymentRequestDTO("TX-B", PaymentSource.CREDIT_CARD, new BigDecimal("150.00"), "PAYER-B"), PaymentSource.CREDIT_CARD),
                arguments(new PaymentRequestDTO("TX-C", PaymentSource.DEBIT_CARD, new BigDecimal("75.00"), "PAYER-C"), PaymentSource.DEBIT_CARD)
            );
        }
    }
}
