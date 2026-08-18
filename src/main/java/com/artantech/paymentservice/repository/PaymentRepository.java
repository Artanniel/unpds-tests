package com.artantech.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.model.PaymentSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByCreatedAtAsc();

    List<Payment> findByPayerIdOrderByCreatedAtAsc(String payerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentSource = :source AND p.createdAt >= :startOfDay AND p.createdAt <= :endOfDay")
    BigDecimal sumAmountByPaymentSourceAndDateRange(
            @Param("source") PaymentSource source,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}
