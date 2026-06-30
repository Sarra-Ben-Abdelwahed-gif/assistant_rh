package com.example.assistant_rh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientLeaveBalanceException
        extends RuntimeException {
    public InsufficientLeaveBalanceException(
            int balance, long requested) {
        super("Insufficient balance : " + balance
            + " day(s) available, "
            + requested + " day(s) requested");
    }
}