package com.artantech.paymentservice.exceptions;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long id) {
        super("Payment not found with ID: " + id);
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
