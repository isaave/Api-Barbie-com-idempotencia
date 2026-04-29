package com.barbie.dreamworld_api.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Envelope padrão de resposta de erro da API.
 *
 * Exemplo — 404:
 * { "timestamp":"...", "status":404, "error":"Not Found",
 *   "message":"Barbie com id 99 não encontrada.", "path":"/api/v1/barbies/99" }
 *
 * Exemplo — 400 (validação):
 * { ..., "fieldErrors": [{ "field":"nome", "message":"não pode estar em branco" }] }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldErrorDetail> fieldErrors;

    @Getter
    @Builder
    public static class FieldErrorDetail {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}
