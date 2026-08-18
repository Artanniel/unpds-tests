package com.artantech.paymentservice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.artantech.paymentservice.controller.PayerController;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.exceptions.GlobalExceptionHandler;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;
import com.artantech.paymentservice.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayerController.class)
@Import(GlobalExceptionHandler.class)
class PayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("GET /payer/{id} - Deve retornar os pagamentos de um pagador existente")
    void shouldGetPaymentsByPayerId() throws Exception {
        PaymentResponseDTO dto = new PaymentResponseDTO(1L, "TX-100", PaymentSource.PIX, new BigDecimal("150.00"),
                PaymentStatus.PENDING, "PAYER-123", LocalDateTime.now());
        when(paymentService.getPaymentsByPayerId("PAYER-123")).thenReturn(List.of(dto));

        mockMvc.perform(get("/payer/PAYER-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].payerId", is("PAYER-123")))
                .andExpect(jsonPath("$[0].transactionId", is("TX-100")));
    }

    @Test
    @DisplayName("GET /payer/{id} - Deve retornar lista vazia HTTP 200 para pagador sem transações")
    void shouldReturnEmptyListWhenPayerHasNoPayments() throws Exception {
        when(paymentService.getPaymentsByPayerId("PAYER-UNKNOWN")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/payer/PAYER-UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }
}
