package com.barbie.dreamworld_api.exceptions;

/** HTTP 422 — violação de regra de negócio. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
