package com.example.reservation.job;

import com.example.reservation.entity.Availability;
import com.example.reservation.service.AvailabilityService;
import com.example.reservation.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Slf4j
/**
 * Scheduled job to generate availability entries for each location up to 1 month from today's date
 */
public class AvailabilityConfigurationJob {

    private final LocationService locationService;

    private final AvailabilityService availabilityService;

    public AvailabilityConfigurationJob(final LocationService locationService, final AvailabilityService availabilityService) {
        this.locationService = locationService;
        this.availabilityService = availabilityService;
    }

    @PostConstruct
    public void startupAvailabilityCreation() {
        dailyAvailabilityJob();
    }

    @Scheduled(cron = "0 0 0 ? * *")
    public void dailyAvailabilityJob() {
        log.info("Running daily availability job");
        LocalDate today = LocalDate.now();
        LocalDate lastReservableDate = LocalDate.now().plusMonths(1);
        // Get all locations from the location table
        locationService.findAllLocations().stream()
                // Fetch the latest availability for each location
                .map(location -> Pair.of(location.getLocationId(), availabilityService.findLatestAvailabilityFor(location.getLocationId())))
                // Create pairs of the location and the first reservable date that doesn't have availability (default to tomorrow's date if there is no availability)
                .map((locationAvailabilityPair) -> {
                    String locationId = locationAvailabilityPair.getFirst();
                    Optional<Availability> optionalAvailability = locationAvailabilityPair.getSecond();
                    return optionalAvailability.map(availability ->
                            Pair.of(locationId, availability.getReservationDate().plusDays(1)))
                            .orElseGet(() -> Pair.of(locationId, today.plusDays(1)));
                })
                // Filter for pairs where we need to create availability entries
                .filter(locationDatePair -> !locationDatePair.getSecond().isAfter(lastReservableDate))
                // Generate the individual location and date pairs for which availability entries need to be created
                .flatMap(locationDatePair -> {
                    LocalDate firstDateToNeedAvailability = locationDatePair.getSecond();
                    return firstDateToNeedAvailability.datesUntil(lastReservableDate.plusDays(1))
                            .map(date -> Pair.of(locationDatePair.getFirst(), date));

                })
                // Create an availability fo each location and date pair
                .forEach(locationDatePair -> {
                    String locationId = locationDatePair.getFirst();
                    LocalDate reservationDate = locationDatePair.getSecond();
                    Availability availability = Availability.builder()
                            .locationId(locationId)
                            .reservationDate(reservationDate)
                            .createdTime(LocalDateTime.now())
                            .lastModifiedTime(LocalDateTime.now())
                            .build();
                    log.debug("Creating availability for locationId={} and reservationDate={}", locationId,
                            reservationDate);
                    availabilityService.persist(availability);
                    log.debug("Successfully created availability for locationId={} and resevationDate={}", locationId,
                            reservationDate);
                });
    }

}
