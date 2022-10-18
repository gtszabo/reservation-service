package com.example.reservation.configuration;

import com.example.reservation.exception.InvalidReservationException;
import com.example.reservation.exception.LocationUnavailableException;
import com.example.reservation.exception.ReservationNotFoundException;
import com.example.reservation.model.ExceptionResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
public class ReservationControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidReservationException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidReservation(InvalidReservationException ex) {
        ExceptionResponse er = ExceptionResponse.builder()
                .errors(ex.getErrors())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(er);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleReservationNotFound(ReservationNotFoundException ex) {
        ExceptionResponse er = ExceptionResponse.builder().message(ex.getMessage()).build();
        return ResponseEntity.badRequest().body(er);
    }

    @ExceptionHandler(LocationUnavailableException.class)
    public ResponseEntity<ExceptionResponse> handleLocationUnavailable(LocationUnavailableException ex) {
        ExceptionResponse er = ExceptionResponse.builder().message(ex.getMessage()).build();
        return ResponseEntity.badRequest().body(er);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errorList = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        ExceptionResponse er = ExceptionResponse.builder()
                .message("Invalid arguments provided")
                .errors(errorList)
                .build();
        return ResponseEntity.badRequest().body(er);
    }
}
