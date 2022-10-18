package com.example.reservation.service;

import com.example.reservation.entity.Location;
import com.example.reservation.exception.InvalidLocationException;
import com.example.reservation.repository.LocationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Random;

import static org.mockito.ArgumentMatchers.anyString;

public class LocationServiceTest {

    Random random = new Random();

    @InjectMocks
    LocationService locationService;

    @Mock
    LocationRepository locationRepository;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void findBy_locationNotFound() {
        Mockito.when(locationRepository.findByLocationId(anyString())).thenReturn(Optional.empty());

        Assertions.assertThrows(InvalidLocationException.class, () -> locationService.findBy("DUMMY"));
    }

    @Test
    public void findBy_locationFound() {
        String locationId = "LOCATION_ID";
        Location location = Location.builder()
                .id(random.nextLong())
                .locationId(locationId)
                .build();
        Mockito.when(locationRepository.findByLocationId(locationId)).thenReturn(Optional.of(location));

        Assertions.assertEquals(location, locationService.findBy(locationId));
    }
}
