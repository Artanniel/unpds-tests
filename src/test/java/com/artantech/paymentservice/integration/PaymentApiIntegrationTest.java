package com.artantech.paymentservice.integration;

import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.dto.PaymentStatusUpdateDTO;
import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.model.PaymentStatus;
import com.artantech.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("mysql")
class PaymentApiIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private PaymentRepository paymentRepository;

        @BeforeEach
        void setUp() {
                paymentRepository.deleteAll();
        }

        @Test
        @DisplayName("E2E - Deve criar pagamento no banco de dados com status PENDING e retornar HTTP 201")
        void createPayment() throws Exception {
                String payload = """
                                {
                                    "payerId": "f5d19454-ccb0-4de0-be78-b25b8a442464",
                                    "paymentSource": "PIX",
                                    "amount": 100.50
                                }
                                """;

                mockMvc.perform(
                                post("/payments")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(payload))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.payerId", is("f5d19454-ccb0-4de0-be78-b25b8a442464")))
                                .andExpect(jsonPath("$.paymentSource", is(PaymentSource.PIX.name())))
                                .andExpect(jsonPath("$.amount", is(100.50)))
                                .andExpect(jsonPath("$.status", is(PaymentStatus.PENDING.name())));
        }

        @Test
        @DisplayName("E2E - Deve criar pagamento utilizando Object Mapping na requisicao e na resposta (ObjectMapper + AssertJ)")
        void createPaymentWithObjectMapping() throws Exception {
                PaymentRequestDTO paymentRequest = new PaymentRequestDTO(
                                "TX-MAPPER-01",
                                PaymentSource.PIX,
                                new BigDecimal("100.50"),
                                "f5d19454-ccb0-4de0-be78-b25b8a442464");

                String responseInJson = mockMvc.perform(
                                post("/payments")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(paymentRequest)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                PaymentResponseDTO paymentResponse = objectMapper.readValue(responseInJson, PaymentResponseDTO.class);

                assertThat(paymentResponse.payerId()).isEqualTo(paymentRequest.payerId());
                assertThat(paymentResponse.paymentSource()).isEqualTo(paymentRequest.paymentSource());
                assertThat(paymentResponse.amount()).isEqualByComparingTo(paymentRequest.amount());
                assertThat(paymentResponse.status()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("E2E - Deve barrar com HTTP 422 quando a soma dos pagamentos no dia exceder R$ 2.000,00 para a mesma fonte")
        void shouldEnforceDailyLimitInDatabase() throws Exception {
                // Criar primeiro pagamento de R$ 1.500,00 (Sucesso)
                PaymentRequestDTO firstPayment = new PaymentRequestDTO("TX-LIMIT-1", PaymentSource.CREDIT_CARD,
                                new BigDecimal("1500.00"), "PAYER-1");
                mockMvc.perform(post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstPayment)))
                                .andExpect(status().isCreated());

                // Criar segundo pagamento de R$ 600,00 (Soma = R$ 2.100,00 -> Excede limite)
                PaymentRequestDTO secondPayment = new PaymentRequestDTO("TX-LIMIT-2", PaymentSource.CREDIT_CARD,
                                new BigDecimal("600.00"), "PAYER-1");
                mockMvc.perform(post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(secondPayment)))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.message", containsString("Daily limit of 2000.00 exceeded")));

                // Verifica se apenas 1 pagamento foi de fato salvo no banco de dados
                assertThat(paymentRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("E2E - Deve consultar pagamento existente por ID via GET /payments/{id}")
        void getPaymentById() throws Exception {
                // precondicao para o teste
                Payment payment = new Payment(null, "TX-100", PaymentSource.PIX, new BigDecimal("100.00"),
                                PaymentStatus.PENDING, "PAYER-1", null);

                Payment savedPayment = paymentRepository.save(payment);

                mockMvc.perform(get("/payments/{paymentId}", savedPayment.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.payerId", is(savedPayment.getPayerId())))
                                .andExpect(jsonPath("$.paymentSource", is(savedPayment.getPaymentSource().name())))
                                .andExpect(jsonPath("$.amount", is(savedPayment.getAmount().doubleValue())))
                                .andExpect(jsonPath("$.status", is(savedPayment.getStatus().name())));
        }

        @Test
        @DisplayName("E2E - Deve retornar HTTP 404 ao buscar ID inexistente no banco")
        void shouldReturn404ForNonExistingPayment() throws Exception {
                mockMvc.perform(get("/payments/9999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message", is("Payment not found with ID: 9999")));
        }

        @Test
        @DisplayName("E2E - Deve atualizar status no banco e barrar transição inválida (PAID -> PENDING) com HTTP 409")
        void shouldUpdateStatusAndPreventInvalidTransitionInDatabase() throws Exception {
                // 1. Criar pagamento
                PaymentRequestDTO requestDTO = new PaymentRequestDTO("TX-UPD-01", PaymentSource.PIX,
                                new BigDecimal("150.00"), "PAYER-3");
                String content = mockMvc.perform(post("/payments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                Long paymentId = objectMapper.readTree(content).get("id").asLong();

                // 2. Atualizar status de PENDING para PAID
                PaymentStatusUpdateDTO updatePaid = new PaymentStatusUpdateDTO(PaymentStatus.PAID);
                mockMvc.perform(put("/payments/" + paymentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePaid)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("PAID")));

                // Verificar persistência no banco
                assertThat(paymentRepository.findById(paymentId).orElseThrow().getStatus())
                                .isEqualTo(PaymentStatus.PAID);

                // 3. Tentar transição proibida: PAID de volta para PENDING -> HTTP 409 Conflict
                PaymentStatusUpdateDTO updatePending = new PaymentStatusUpdateDTO(PaymentStatus.PENDING);
                mockMvc.perform(put("/payments/" + paymentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePending)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message",
                                                containsString("Cannot change status from PAID back to PENDING")));
        }

        @Test
        @DisplayName("E2E - Deve listar todos os pagamentos (validacao posicional e generica)")
        void getAllPayments() throws Exception {
                Payment p1 = paymentRepository.save(new Payment(null, "TX-1", PaymentSource.PIX, new BigDecimal("100.00"),
                                PaymentStatus.PENDING, "PAYER-1", null));
                Payment p2 = paymentRepository.save(new Payment(null, "TX-2", PaymentSource.DEBIT_CARD, new BigDecimal("200.00"),
                                PaymentStatus.PENDING, "PAYER-1", null));

                // Validacao Posicional e Generica com JSONPath
                mockMvc.perform(get("/payments"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                // Validacao Posicional [0]
                                .andExpect(jsonPath("$[0].id", is(p1.getId().intValue())))
                                .andExpect(jsonPath("$[0].payerId", is(p1.getPayerId())))
                                .andExpect(jsonPath("$[0].amount", is(p1.getAmount().doubleValue())))
                                // Validacao Posicional [1]
                                .andExpect(jsonPath("$[1].id", is(p2.getId().intValue())))
                                .andExpect(jsonPath("$[1].payerId", is(p2.getPayerId())))
                                .andExpect(jsonPath("$[1].amount", is(p2.getAmount().doubleValue())))
                                // Validacao Generica com Matchers (Wildcard $[*])
                                .andExpect(jsonPath("$[*].payerId", everyItem(is("PAYER-1"))))
                                .andExpect(jsonPath("$[*].amount", containsInAnyOrder(100.00, 200.00)));
        }

        @Test
        @DisplayName("E2E - Deve listar pagamentos desserializando em List<PaymentResponseDTO> com ObjectMapper (getTypeFactory)")
        void getAllPaymentsWithObjectMapperCollectionType() throws Exception {
                Payment p1 = paymentRepository.save(new Payment(null, "TX-100", PaymentSource.PIX, new BigDecimal("100.00"),
                                PaymentStatus.PENDING, "PAYER-1", null));
                Payment p2 = paymentRepository.save(new Payment(null, "TX-200", PaymentSource.DEBIT_CARD, new BigDecimal("200.00"),
                                PaymentStatus.PENDING, "PAYER-1", null));

                String responseInJson = mockMvc.perform(get("/payments"))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                List<PaymentResponseDTO> listOfPaymentResponse = objectMapper.readValue(
                                responseInJson,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, PaymentResponseDTO.class)
                );

                assertThat(listOfPaymentResponse).hasSize(2);
                assertThat(listOfPaymentResponse.get(0).payerId()).isEqualTo("PAYER-1");
                assertThat(listOfPaymentResponse.get(1).payerId()).isEqualTo("PAYER-1");
                assertThat(listOfPaymentResponse)
                                .extracting(PaymentResponseDTO::amount)
                                .containsExactlyInAnyOrder(new BigDecimal("100.00"), new BigDecimal("200.00"));
        }
}
