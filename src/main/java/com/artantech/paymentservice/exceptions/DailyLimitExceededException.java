package com.artantech.paymentservice.exceptions;

import java.math.BigDecimal;

import com.artantech.paymentservice.model.PaymentSource;

public class DailyLimitExceededException extends PaymentLimitException {

    public DailyLimitExceededException(PaymentSource source, BigDecimal limit, BigDecimal currentTotal,
            BigDecimal attempted) {
        super(String.format("Daily limit of %.2f exceeded for payment source %s. Current total: %.2f, Attempted: %.2f",
                limit, source, currentTotal, attempted));
    }

    public DailyLimitExceededException(String message) {
        super(message);
    }
}
