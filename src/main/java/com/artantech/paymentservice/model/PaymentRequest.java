package com.artantech.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Modelo de requisição de pagamento para uso com o Test Data Factory.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private String transactionId;
    private String payerId;
    private PaymentSource paymentSource;
    private BigDecimal amount;

    public static class PaymentRequestBuilder {
        public PaymentRequestBuilder payerId(UUID payerId) {
            this.payerId = payerId != null ? payerId.toString() : null;
            return this;
        }

        public PaymentRequestBuilder payerId(String payerId) {
            this.payerId = payerId;
            return this;
        }
    }
}
