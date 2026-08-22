package com.artantech.paymentservice.data;

import com.artantech.paymentservice.model.PaymentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentDataFactoryTest {

    @Test
    @DisplayName("Deve gerar uma solicitação de pagamento válida com o PaymentDataFactory")
    void deveGerarPaymentRequestValido() {
        PaymentRequest request = PaymentDataFactory.validPaymentRequest();

        assertThat(request).isNotNull();
        assertThat(request.getPayerId()).isNotBlank();
        assertThat(request.getPaymentSource()).isNotNull();
        assertThat(request.getAmount()).isBetween(BigDecimal.valueOf(1L), BigDecimal.valueOf(2000L));
    }

    @Test
    @DisplayName("Deve gerar uma solicitação de pagamento inválida (amount > 2000) com o PaymentDataFactory")
    void deveGerarPaymentRequestInvalido() {
        PaymentRequest request = PaymentDataFactory.invalidPaymentRequest();

        assertThat(request).isNotNull();
        assertThat(request.getPayerId()).isNotBlank();
        assertThat(request.getPaymentSource()).isNotNull();
        assertThat(request.getAmount()).isGreaterThan(BigDecimal.valueOf(2000L));
    }
}
