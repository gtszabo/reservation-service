package com.example.reservation.service;

import com.example.reservation.entity.Availability;
import com.example.reservation.exception.LocationUnavailableException;
import com.example.reservation.repository.AvailabilityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AvailabilityService {

    private AvailabilityRepository availabilityRepository;

    @Autowired
    public AvailabilityService(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    public Availability persist(Availability availability) {
        return availabilityRepository.save(availability);
    }
    public Optional<Availability> findLatestAvailabilityFor(String locationId) {
        return availabilityRepository.findLatestAvailabilityForLocation(locationId);
    }

    public List<Availability> findFreeAvailabilityFor(String locationId, LocalDate startDate, LocalDate endDate) {
        return availabilityRepository.findAllFreeAvailability(locationId, startDate, endDate);
    }

    public List<Availability> findFreeAvailabilityToReserveFor(String locationId, List<LocalDate> dateList) {
        List<Availability> availabilityList = availabilityRepository.findAllFreeAvailabilityToReserve(locationId, dateList);
        if (availabilityList.size() != dateList.size()) {
            throw new LocationUnavailableException(String.format("Location=%s not available for the provided dates=%s",
                    locationId, dateList));
        }
        return availabilityList;
    }

    public List<Availability> updateAvailabilityFor(String reservationId, List<Availability> availabilityList) {
        return availabilityList.stream().map(availability -> {
            availability.setReservationId(reservationId);
            availability.setLastModifiedTime(LocalDateTime.now());
            return availabilityRepository.save(availability);
        }).toList();
    }

    public List<Availability> releaseAvailabilityFor(String locationId, List<LocalDate> releaseDates) {
        return updateAvailabilityFor(null, availabilityRepository.findAllToUpdate(locationId, releaseDates));
    }

    public List<Availability> releaseAvailabilityFor(String reservationId) {
        return updateAvailabilityFor(null, availabilityRepository.findAllToUpdate(reservationId));
    }
}
