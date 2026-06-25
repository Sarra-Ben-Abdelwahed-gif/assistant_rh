package com.example.assistant_rh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException
        extends RuntimeException {
    public UnauthorizedAccessException() {
        super("Accès non autorisé");
    }
}