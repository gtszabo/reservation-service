package com.example.reservation.entity;

import com.example.reservation.exception.InvalidReservationException;
import com.example.reservation.model.ReservationRequest;
import com.example.reservation.model.ReservationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    public static final int MAX_RESERVATION_DAYS = 3;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String reservationId;

    private String firstName;

    private String lastName;

    private String email;

    private String locationId;
    private LocalDate arrival;

    private LocalDate departure;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdTime;

    private LocalDateTime lastModifiedTime;

    public enum Status {
        CONFIRMED,
        CANCELLED
    }

    public Reservation(ReservationRequest request) {
        this.reservationId = UUID.randomUUID().toString();
        this.firstName = request.getFirstName();
        this.lastName = request.getLastName();
        this.email = request.getEmail();
        this.locationId = request.getLocationId();
        this.arrival = request.getArrival();
        this.departure = request.getDeparture();
        this.status = Status.CONFIRMED;
        this.createdTime = LocalDateTime.now();
        this.lastModifiedTime = LocalDateTime.now();
    }

    public void validate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate lastReservableDate = LocalDate.now().plusMonths(1);
        List<String> errors = new ArrayList<>();
        if (arrival.isBefore(tomorrow)) {
            errors.add("Arrival date must be after today");
        }
        if (departure.isBefore(tomorrow)) {
            errors.add("Departure date must be after today");
        }
        if (arrival.isAfter(lastReservableDate)) {
            errors.add(String.format("Arrival date must be before or on the last reservable date(%s)",
                    lastReservableDate));
        }
        if (departure.isAfter(lastReservableDate)) {
            errors.add(String.format("Departure date must be before or on the last reservable date(%s)",
                    lastReservableDate));
        }
        if (arrival.isAfter(departure)) {
            errors.add("Departure date must be before or on arrival date");
        }
        if (ChronoUnit.DAYS.between(arrival, departure) >= MAX_RESERVATION_DAYS) {
            errors.add(String.format("Reservation can only be made for a maximum of %s days", MAX_RESERVATION_DAYS));
        }
        if (!errors.isEmpty()) {
            throw new InvalidReservationException(errors, "Invalid reservation");
        }
    }
    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public ReservationResponse transform() {
        return ReservationResponse.builder()
                .reservationId(reservationId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .locationId(locationId)
                .arrival(arrival)
                .departure(departure)
                .status(status)
                .build();
    }
}
