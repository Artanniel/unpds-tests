package com.artantech.paymentservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.dto.PaymentStatusUpdateDTO;
import com.artantech.paymentservice.exceptions.PaymentNotFoundException;
import com.artantech.paymentservice.model.Payment;
import com.artantech.paymentservice.repository.PaymentRepository;
import com.artantech.paymentservice.service.PaymentService;
import com.artantech.paymentservice.validator.DailyLimitValidator;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final DailyLimitValidator dailyLimitValidator;

    public PaymentServiceImpl(PaymentRepository paymentRepository, DailyLimitValidator dailyLimitValidator) {
        this.paymentRepository = paymentRepository;
        this.dailyLimitValidator = dailyLimitValidator;
    }

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO) {
        dailyLimitValidator.validateDailyLimit(requestDTO.paymentSource(), requestDTO.amount());

        String txId = (requestDTO.transactionId() != null && !requestDTO.transactionId().isBlank())
                ? requestDTO.transactionId()
                : "TX-" + java.util.UUID.randomUUID().toString().substring(0, 8);

        Payment payment = new Payment(
                txId,
                requestDTO.paymentSource(),
                requestDTO.amount(),
                requestDTO.payerId());

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponseDTO.fromEntity(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return PaymentResponseDTO.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtAsc()
                .stream()
                .map(PaymentResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByPayerId(String payerId) {
        return paymentRepository.findByPayerIdOrderByCreatedAtAsc(payerId)
                .stream()
                .map(PaymentResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatusUpdateDTO statusDTO) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        payment.updateStatus(statusDTO.status());

        Payment updatedPayment = paymentRepository.save(payment);
        return PaymentResponseDTO.fromEntity(updatedPayment);
    }
}
