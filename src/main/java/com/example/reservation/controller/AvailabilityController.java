package com.example.reservation.controller;

import com.example.reservation.model.AvailabilityResponse;
import com.example.reservation.service.AvailabilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(final AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("")
    public AvailabilityResponse findAvailability(
            @RequestParam String locationId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().plusDays(1)}") LocalDate startDate,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().plusMonths(1)}") LocalDate endDate) {
        List<LocalDate> freeAvailability = new ArrayList<>();
        availabilityService.findFreeAvailabilityFor(locationId, startDate, endDate)
                .forEach(availability -> freeAvailability.add(availability.getReservationDate()));
        return AvailabilityResponse.builder()
                .locationId(locationId)
                .availableDates(freeAvailability)
                .build();
    }
}
