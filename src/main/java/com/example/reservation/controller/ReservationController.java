package com.example.reservation.controller;

import com.example.reservation.facade.ReservationFacade;
import com.example.reservation.model.ReservationRequest;
import com.example.reservation.model.ReservationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/reservations")
@Validated
public class ReservationController {

    private final ReservationFacade reservationFacade;

    public ReservationController(final ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    @PostMapping("")
    public ResponseEntity<ReservationResponse> registerReservation(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationFacade.placeReservation(request));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable String reservationId) {
        return ResponseEntity.ok(reservationFacade.retrieveReservation(reservationId));
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> updateReservation(@PathVariable String reservationId,
                                                 @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationFacade.updateReservation(reservationId, request));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> deleteReservation(@PathVariable String reservationId) {
        return ResponseEntity.ok(reservationFacade.cancelReservation(reservationId));
    }
}
