package com.artantech.paymentservice.integration;

import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:integrationdb;DB_CLOSE_DELAY=-1;MODE=LEGACY",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PayerApiIntegrationTest {

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
    @DisplayName("E2E - Deve listar pagamentos de um pagador específico via GET /payer/{id}")
    void shouldGetPaymentsByPayerIdFromDatabase() throws Exception {
        // Cadastrar pagamentos para PAYER-CLIENTE-X
        PaymentRequestDTO p1 = new PaymentRequestDTO("TX-PAYER-1", PaymentSource.PIX, new BigDecimal("100.00"),
                "PAYER-CLIENTE-X");
        PaymentRequestDTO p2 = new PaymentRequestDTO("TX-PAYER-2", PaymentSource.CREDIT_CARD, new BigDecimal("200.00"),
                "PAYER-CLIENTE-X");
        // Cadastrar pagamento para outro pagador
        PaymentRequestDTO p3 = new PaymentRequestDTO("TX-PAYER-3", PaymentSource.DEBIT_CARD, new BigDecimal("300.00"),
                "PAYER-OUTRO");

        mockMvc.perform(
                post("/payments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isCreated());
        mockMvc.perform(
                post("/payments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isCreated());
        mockMvc.perform(
                post("/payments").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p3)))
                .andExpect(status().isCreated());

        // Consultar pagamentos do PAYER-CLIENTE-X
        mockMvc.perform(get("/payer/PAYER-CLIENTE-X"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].payerId", is("PAYER-CLIENTE-X")))
                .andExpect(jsonPath("$[1].payerId", is("PAYER-CLIENTE-X")));
    }

    @Test
    @DisplayName("E2E - Deve retornar lista vazia quando o pagador não possuir pagamentos cadastrados")
    void shouldReturnEmptyListWhenPayerHasNoPayments() throws Exception {
        mockMvc.perform(get("/payer/PAYER-INEXISTENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
