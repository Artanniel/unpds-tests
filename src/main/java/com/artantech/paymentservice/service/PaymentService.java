package com.artantech.paymentservice.service;

import java.util.List;

import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.dto.PaymentStatusUpdateDTO;

public interface PaymentService {

    PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO);

    PaymentResponseDTO getPaymentById(Long id);

    List<PaymentResponseDTO> getAllPayments();

    List<PaymentResponseDTO> getPaymentsByPayerId(String payerId);

    PaymentResponseDTO updatePaymentStatus(Long id, PaymentStatusUpdateDTO statusDTO);
}
