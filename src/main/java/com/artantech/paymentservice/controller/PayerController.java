package com.artantech.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/payer")
@Tag(name = "Payers", description = "Endpoints para consulta de pagamentos por pagador")
public class PayerController {

    private final PaymentService paymentService;

    public PayerController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pagamentos por ID do pagador", description = "Retorna todos os pagamentos realizados por um pagador específico, ordenados por data de criação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pagamentos do pagador retornada com sucesso", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentResponseDTO.class))))
    })
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByPayerId(@PathVariable("id") String payerId) {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByPayerId(payerId);
        return ResponseEntity.ok(payments);
    }
}
