package com.barbie.dreamworld_api.exceptions;

/** HTTP 409 — conflito de unicidade. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
