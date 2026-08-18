package com.artantech.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> errors) {
    public ErrorResponseDTO {
        errors = errors != null ? Map.copyOf(errors) : null;
    }

    public ErrorResponseDTO(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, null);
    }

    public ErrorResponseDTO(int status, String error, String message, Map<String, String> errors) {
        this(LocalDateTime.now(), status, error, message, errors);
    }

    @Override
    public Map<String, String> errors() {
        return errors != null ? Map.copyOf(errors) : null;
    }
}
