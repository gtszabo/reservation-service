package com.example.reservation.model;

import com.example.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private String reservationId;

    private String firstName;

    private String lastName;

    private String email;

    private String locationId;

    private LocalDate arrival;

    private LocalDate departure;

    private Reservation.Status status;
}
