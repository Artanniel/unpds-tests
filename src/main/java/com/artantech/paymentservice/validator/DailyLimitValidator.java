package com.artantech.paymentservice.validator;

import org.springframework.stereotype.Component;

import com.artantech.paymentservice.exceptions.DailyLimitExceededException;
import com.artantech.paymentservice.exceptions.PaymentLimitException;
import com.artantech.paymentservice.model.PaymentSource;
import com.artantech.paymentservice.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DailyLimitValidator {

    public static final BigDecimal DAILY_LIMIT = new BigDecimal("2000.00");

    private final PaymentRepository paymentRepository;

    public DailyLimitValidator(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void validateDailyLimit(PaymentSource source, BigDecimal newAmount) {
        if (source == null || newAmount == null) {
            return;
        }

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentLimitException("Payment amount must be greater than zero");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal currentTotal = paymentRepository.sumAmountByPaymentSourceAndDateRange(source, startOfDay, endOfDay);
        if (currentTotal == null) {
            currentTotal = BigDecimal.ZERO;
        }

        BigDecimal projectTotal = currentTotal.add(newAmount);

        if (projectTotal.compareTo(DAILY_LIMIT) > 0) {
            throw new DailyLimitExceededException(source, DAILY_LIMIT, currentTotal, newAmount);
        }
    }

    /**
     * Versão booleana de {@link #validateDailyLimit(PaymentSource, BigDecimal)}.
     * Encapsula a validação existente (baseada em exceção) e traduz o resultado
     * em um boolean: {@code true} quando o valor viola alguma regra do limite
     * diário (valor inválido ou limite excedido — ex.: {@link PaymentLimitException}),
     * {@code false} quando o pagamento é válido e respeita o limite.
     */
    public boolean isLimitExceeded(PaymentSource source, BigDecimal newAmount) {
        try {
            validateDailyLimit(source, newAmount);
            return false;
        } catch (PaymentLimitException e) {
            return true;
        }
    }
}
