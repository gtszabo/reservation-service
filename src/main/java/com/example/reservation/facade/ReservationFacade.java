package com.example.reservation.facade;

import com.example.reservation.entity.Availability;
import com.example.reservation.entity.Reservation;
import com.example.reservation.model.ReservationDifference;
import com.example.reservation.model.ReservationRequest;
import com.example.reservation.model.ReservationResponse;
import com.example.reservation.service.AvailabilityService;
import com.example.reservation.service.LocationService;
import com.example.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ReservationFacade {

    private final AvailabilityService availabilityService;

    private final ReservationService reservationService;

    private final LocationService locationService;

    @Autowired
    public ReservationFacade(final AvailabilityService availabilityService, ReservationService reservationService,
                             LocationService locationService) {
        this.availabilityService = availabilityService;
        this.reservationService = reservationService;
        this.locationService = locationService;
    }

    @Transactional
    public ReservationResponse placeReservation(ReservationRequest request) {
        locationService.findBy(request.getLocationId());

        List<Availability> availabilityList = availabilityService.findFreeAvailabilityToReserveFor(request.getLocationId(),
                request.getArrival().datesUntil(request.getDeparture().plusDays(1)).toList());
        Reservation reservation = new Reservation(request);
        reservation.validate();
        availabilityService.updateAvailabilityFor(reservation.getReservationId(), availabilityList);
        return reservationService.persist(reservation).transform();
    }

    @Transactional
    public ReservationResponse updateReservation(String reservationId, ReservationRequest request) {

        Reservation existingReservation = reservationService.retrieve(reservationId);
        Reservation reservation = new Reservation(request);
        reservation.validate();
        reservation.setId(existingReservation.getId());
        reservation.setReservationId(existingReservation.getReservationId());
        locationService.findBy(reservation.getLocationId());

        ReservationDifference difference = reservationService.determineReservationDelta(
                existingReservation.getArrival(), existingReservation.getDeparture(), reservation.getArrival(),
                reservation.getDeparture());
        List<Availability> availabilityList = availabilityService.findFreeAvailabilityToReserveFor(
                reservation.getLocationId(), difference.getDatesToAdd());
        availabilityService.updateAvailabilityFor(reservationId, availabilityList);
        availabilityService.releaseAvailabilityFor(existingReservation.getLocationId(), difference.getDatesToRemove());

        return reservationService.persist(reservation).transform();
    }

    public ReservationResponse retrieveReservation(String reservationId) {
        return reservationService.retrieve(reservationId).transform();
    }

    @Transactional
    public ReservationResponse cancelReservation(String reservationId) {
        availabilityService.releaseAvailabilityFor(reservationId);
        return reservationService.cancel(reservationId).transform();
    }

}
