package com.example.reservation.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReservationDifference {
    private List<LocalDate> datesToRemove;
    private List<LocalDate> datesToAdd;
}
