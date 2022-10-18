package com.example.reservation.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidReservationException extends RuntimeException {

    private List<String> errors;
    public InvalidReservationException(List<String> errors, String messages) {
        super(messages);
        this.errors = errors;
    }
}
