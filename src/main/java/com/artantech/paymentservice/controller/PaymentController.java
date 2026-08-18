package com.artantech.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artantech.paymentservice.dto.ErrorResponseDTO;
import com.artantech.paymentservice.dto.PaymentRequestDTO;
import com.artantech.paymentservice.dto.PaymentResponseDTO;
import com.artantech.paymentservice.dto.PaymentStatusUpdateDTO;
import com.artantech.paymentservice.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Endpoints para gerenciamento e processamento de pagamentos")
public class PaymentController {

        private final PaymentService paymentService;

        public PaymentController(PaymentService paymentService) {
                this.paymentService = paymentService;
        }

        @PostMapping
        @Operation(summary = "Criar novo pagamento", description = "Cria um novo pagamento com status inicial PENDING respeitando o limite diário acumulado de R$ 2.000,00 por fonte de pagamento.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso", content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos (campos obrigatórios ausentes ou valores negativos)", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "422", description = "Limite diário acumulado de R$ 2.000,00 excedido para a fonte de pagamento", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
        })
        public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
                PaymentResponseDTO responseDTO = paymentService.createPayment(requestDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Consultar pagamento por ID", description = "Retorna as informações detalhadas de um pagamento a partir do seu ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Pagamento encontrado com sucesso", content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
        })
        public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
                PaymentResponseDTO responseDTO = paymentService.getPaymentById(id);
                return ResponseEntity.ok(responseDTO);
        }

        @GetMapping
        @Operation(summary = "Listar todos os pagamentos", description = "Retorna a lista completa de pagamentos ordenados por data de criação.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lista de pagamentos retornada com sucesso", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentResponseDTO.class))))
        })
        public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
                List<PaymentResponseDTO> payments = paymentService.getAllPayments();
                return ResponseEntity.ok(payments);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Atualizar status do pagamento", description = "Atualiza o status de um pagamento existente. Transições a partir de PAID ou FRAUD de volta para PENDING são proibidas.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Status do pagamento atualizado com sucesso", content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                        @ApiResponse(responseCode = "409", description = "Transição de status inválida ou proibida", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
        })
        public ResponseEntity<PaymentResponseDTO> updatePaymentStatus(
                        @PathVariable Long id,
                        @Valid @RequestBody PaymentStatusUpdateDTO statusDTO) {
                PaymentResponseDTO responseDTO = paymentService.updatePaymentStatus(id, statusDTO);
                return ResponseEntity.ok(responseDTO);
        }
}
