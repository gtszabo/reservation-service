package com.example.reservation.service;

import com.example.reservation.entity.Availability;
import com.example.reservation.exception.LocationUnavailableException;
import com.example.reservation.repository.AvailabilityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

public class AvailabilityServiceTest {

    private static final String DEFAULT_LOCATION_ID = "ISL_VOLC_PO";

    Random random = new Random();

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void findFreeAvailabilityToReserveFor_availabilityNotFound() {
        Mockito.when(availabilityRepository.findAllFreeAvailabilityToReserve(anyString(), anyList()))
                .thenReturn(Collections.singletonList(generateAvailability()));

        Assertions.assertThrows(LocationUnavailableException.class,
                () -> availabilityService.findFreeAvailabilityToReserveFor(DEFAULT_LOCATION_ID,
                        List.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))));
    }

    @Test
    public void findFreeAvailabilityToReserveFor_availabilityFound() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Availability firstAvailability = generateAvailability(tomorrow);
        LocalDate dayAfterTomorrow = LocalDate.now().plusDays(2);
        Availability secondAvailability = generateAvailability(dayAfterTomorrow);

        List<Availability> expectedAvailabilityList = List.of(firstAvailability, secondAvailability);
        Mockito.when(availabilityRepository.findAllFreeAvailabilityToReserve(anyString(), anyList()))
                .thenReturn(expectedAvailabilityList);

        List<Availability> availabilityList = availabilityService.findFreeAvailabilityToReserveFor(DEFAULT_LOCATION_ID,
                List.of(tomorrow, dayAfterTomorrow));

        Assertions.assertEquals(expectedAvailabilityList, availabilityList);
    }

    public Availability generateAvailability() {
        return generateAvailability(LocalDate.now().plusDays(random.nextInt(20)));
    }
    public Availability generateAvailability(LocalDate reservationDate) {

        return Availability.builder()
                .id(random.nextLong())
                .reservationDate(reservationDate)
                .locationId(DEFAULT_LOCATION_ID)
                .build();
    }
}
