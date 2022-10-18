package com.example.reservation.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
public class ReservationRequest {

    @NotBlank(message = "First name must not be blank")
    String firstName;

    @NotBlank(message = "Last name must not be blank")
    String lastName;

    @Email(message = "Valid email must be provided")
    String email;

    @NotBlank(message = "Location ID must not be blank")
    String locationId;

    @NotNull(message = "Arrival date must be provided")
    LocalDate arrival;

    @NotNull(message = "Departure date must be provided")
    LocalDate departure;
}
