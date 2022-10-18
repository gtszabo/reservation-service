package com.example.reservation.service;

import com.example.reservation.entity.Location;
import com.example.reservation.exception.InvalidLocationException;
import com.example.reservation.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    private LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<Location> findAllLocations() {
        return locationRepository.findAll();
    }

    public Location findBy(String locationId) {
        return locationRepository.findByLocationId(locationId).orElseThrow(() ->
                new InvalidLocationException(String.format("Location=%s not found", locationId)));
    }

}
