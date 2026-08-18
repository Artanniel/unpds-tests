package com.artantech.paymentservice.controller;

import com.artantech.paymentservice.controller.PaymentController;
import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.dto.PaymentStatusUpdateDTO;
import com.artantech.paymentservice.exceptions.DailyLimitExceededException;
import com.artantech.paymentservice.exceptions.GlobalExceptionHandler;
import com.artantech.paymentservice.exceptions.InvalidStatusTransitionException;
import com.artantech.paymentservice.exceptions.PaymentNotFoundException;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;
import com.artantech.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
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
class PaymentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PaymentService paymentService;

        @Test
        @DisplayName("POST /payments - Deve criar pagamento com sucesso e retornar HTTP 201")
        void shouldCreatePaymentSuccessfully() throws Exception {
                PaymentRequestDTO requestDTO = new PaymentRequestDTO("TX-998811", PaymentSource.PIX,
                                new BigDecimal("250.50"), "PAYER-123");
                PaymentResponseDTO responseDTO = new PaymentResponseDTO(1L, "TX-998811", PaymentSource.PIX,
                                new BigDecimal("250.50"), PaymentStatus.PENDING, "PAYER-123", LocalDateTime.now());

                when(paymentService.createPayment(any(PaymentRequestDTO.class))).thenReturn(responseDTO);

                mockMvc.perform(post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.transactionId", is("TX-998811")))
                                .andExpect(jsonPath("$.paymentSource", is("PIX")))
                                .andExpect(jsonPath("$.amount", is(250.50)))
                                .andExpect(jsonPath("$.status", is("PENDING")))
                                .andExpect(jsonPath("$.payerId", is("PAYER-123")));
        }

        @Test
        @DisplayName("POST /payments - Deve aceitar criacao de pagamento quando transactionId nao for informado")
        void shouldAcceptCreationWhenTransactionIdIsOmitted() throws Exception {
                PaymentResponseDTO responseDTO = new PaymentResponseDTO(1L, "TX-AUTO1234", PaymentSource.PIX,
                                new BigDecimal("100.00"), PaymentStatus.PENDING, "PAYER-123", LocalDateTime.now());

                when(paymentService.createPayment(any(PaymentRequestDTO.class))).thenReturn(responseDTO);

                String jsonPayload = """
                                {
                                  "paymentSource": "PIX",
                                  "amount": 100.00,
                                  "payerId": "PAYER-123"
                                }
                                """;

                mockMvc.perform(post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.transactionId", is("TX-AUTO1234")));
        }

        @Test
        @DisplayName("POST /payments - Deve retornar HTTP 400 quando paymentSource for nulo")
        void shouldReturn400WhenPaymentSourceIsNull() throws Exception {
                String jsonPayload = """
                                {
                                  "transactionId": "TX-100",
                                  "paymentSource": null,
                                  "amount": 100.00,
                                  "payerId": "PAYER-1"
                                }
                                """;

                mockMvc.perform(post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.paymentSource", is("Payment source is required")));
        }

        @Test
        @DisplayName("POST /payments - Deve retornar HTTP 400 quando amount for menor ou igual a zero")
        void shouldReturn400WhenAmountIsZeroOrNegative() throws Exception {
                PaymentRequestDTO requestDTO = new PaymentRequestDTO("TX-100", PaymentSource.PIX,
                                new BigDecimal("0.00"), "PAYER-123");

                mockMvc.perform(post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.amount", is("Amount must be positive")));
        }

        @Test
        @DisplayName("POST /payments - Deve retornar HTTP 422 quando o limite diário for excedido")
        void shouldReturn422WhenDailyLimitExceeded() throws Exception {
                PaymentRequestDTO requestDTO = new PaymentRequestDTO("TX-100", PaymentSource.PIX,
                                new BigDecimal("2500.00"), "PAYER-123");

                when(paymentService.createPayment(any(PaymentRequestDTO.class)))
                                .thenThrow(new DailyLimitExceededException(PaymentSource.PIX, new BigDecimal("2000.00"),
                                                BigDecimal.ZERO, new BigDecimal("2500.00")));

                mockMvc.perform(post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.message", containsString("Daily limit of 2000.00 exceeded")));
        }

        @Test
        @DisplayName("GET /payments/{id} - Deve retornar HTTP 200 quando o pagamento existir")
        void shouldGetPaymentByIdWhenExists() throws Exception {
                PaymentResponseDTO responseDTO = new PaymentResponseDTO(1L, "TX-998811", PaymentSource.PIX,
                                new BigDecimal("250.50"), PaymentStatus.PENDING, "PAYER-123", LocalDateTime.now());
                when(paymentService.getPaymentById(1L)).thenReturn(responseDTO);

                mockMvc.perform(get("/payments/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.transactionId", is("TX-998811")));
        }

        @Test
        @DisplayName("GET /payments/{id} - Deve retornar HTTP 404 quando o pagamento não existir")
        void shouldReturn404WhenPaymentNotFound() throws Exception {
                when(paymentService.getPaymentById(99L)).thenThrow(new PaymentNotFoundException(99L));

                mockMvc.perform(get("/payments/99"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message", is("Payment not found with ID: 99")));
        }

        @Test
        @DisplayName("GET /payments - Deve retornar HTTP 200 com lista vazia quando o banco estiver vazio")
        void shouldReturn200WithEmptyListWhenNoPaymentsExist() throws Exception {
                when(paymentService.getAllPayments()).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/payments"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(0)));
        }

        @Test
        @DisplayName("PUT /payments/{id} - Deve atualizar status para PAID com sucesso")
        void shouldUpdateStatusToPaid() throws Exception {
                PaymentStatusUpdateDTO updateDTO = new PaymentStatusUpdateDTO(PaymentStatus.PAID);
                PaymentResponseDTO responseDTO = new PaymentResponseDTO(1L, "TX-998811", PaymentSource.PIX,
                                new BigDecimal("250.50"), PaymentStatus.PAID, "PAYER-123", LocalDateTime.now());

                when(paymentService.updatePaymentStatus(eq(1L), any(PaymentStatusUpdateDTO.class)))
                                .thenReturn(responseDTO);

                mockMvc.perform(put("/payments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("PAID")));
        }

        @Test
        @DisplayName("PUT /payments/{id} - Deve retornar HTTP 409 ao tentar transição proibida (PAID para PENDING)")
        void shouldReturn409WhenForbiddenStatusTransition() throws Exception {
                PaymentStatusUpdateDTO updateDTO = new PaymentStatusUpdateDTO(PaymentStatus.PENDING);

                when(paymentService.updatePaymentStatus(eq(1L), any(PaymentStatusUpdateDTO.class)))
                                .thenThrow(new InvalidStatusTransitionException(
                                                "Cannot change status from PAID back to PENDING"));

                mockMvc.perform(put("/payments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message", is("Cannot change status from PAID back to PENDING")));
        }
}
