package com.barbie.dreamworld_api.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Intercepta todas as exceções e garante respostas JSON no formato ErrorResponse.
 *
 * Mapeamento:
 *   MethodArgumentNotValidException   → 400 Bad Request
 *   HttpMessageNotReadableException   → 400 Bad Request
 *   ResourceNotFoundException         → 404 Not Found
 *   ConflictException                 → 409 Conflict
 *   BusinessException                 → 422 Unprocessable Entity
 *   Exception (genérico)              → 500 Internal Server Error
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getAllErrors().stream()
                .map(error -> {
                    FieldError fe = (FieldError) error;
                    return ErrorResponse.FieldErrorDetail.builder()
                            .field(fe.getField())
                            .rejectedValue(fe.getRejectedValue())
                            .message(fe.getDefaultMessage())
                            .build();
                }).toList();

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .error("Bad Request")
                        .message("Erro de validação nos campos da requisição.")
                        .path(request.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String message = "Corpo da requisição inválido.";

        Throwable cause = ex.getMostSpecificCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();

            if (causeMessage != null && causeMessage.contains("LocalDate")) {
                message = "Data inválida ou formato incorreto. Use uma data existente no formato YYYY-MM-DD.";
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(400, "Bad Request", message, request));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(404, "Not Found", ex.getMessage(), request));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build(409, "Conflict", ex.getMessage(), request));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(
            RateLimitException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .header("X-Rate-Limit-Retry-After-Seconds", String.valueOf(ex.getRetryAfterSeconds()))
                .body(build(429, "Too Many Requests", ex.getMessage(), request));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(build(422, "Unprocessable Entity", ex.getMessage(), request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(500, "Internal Server Error",
                        "Ocorreu um erro inesperado. Tente novamente mais tarde.", request));
    }

    private ErrorResponse build(int status, String error, String message,
                                HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
    }
}