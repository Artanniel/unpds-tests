package com.artantech.paymentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.artantech.paymentservice.exceptions.InvalidStatusTransitionException;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_source", nullable = false)
    private PaymentSource paymentSource;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "payer_id", nullable = false)
    private String payerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(String transactionId, PaymentSource paymentSource, BigDecimal amount, String payerId) {
        this.transactionId = transactionId;
        this.paymentSource = paymentSource;
        this.amount = amount;
        this.payerId = payerId;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Payment(Long id, String transactionId, PaymentSource paymentSource, BigDecimal amount, PaymentStatus status,
            String payerId, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.paymentSource = paymentSource;
        this.amount = amount;
        this.status = status != null ? status : PaymentStatus.PENDING;
        this.payerId = payerId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void updateStatus(PaymentStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if ((this.status == PaymentStatus.PAID || this.status == PaymentStatus.FRAUD)
                && newStatus == PaymentStatus.PENDING) {
            throw new InvalidStatusTransitionException("Cannot change status from " + this.status + " back to PENDING");
        }
        this.status = newStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentSource getPaymentSource() {
        return paymentSource;
    }

    public void setPaymentSource(PaymentSource paymentSource) {
        this.paymentSource = paymentSource;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id) && Objects.equals(transactionId, payment.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionId);
    }
}
