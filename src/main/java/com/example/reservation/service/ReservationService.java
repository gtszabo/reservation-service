package com.example.reservation.service;

import com.example.reservation.entity.Reservation;
import com.example.reservation.exception.ReservationNotFoundException;
import com.example.reservation.model.ReservationDifference;
import com.example.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    final ReservationRepository reservationRepository;
    @Autowired
    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation persist(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public Reservation retrieve(String reservationId) {
        return reservationRepository.findByReservationIdAndStatus(reservationId, Reservation.Status.CONFIRMED)
                .orElseThrow(() -> new ReservationNotFoundException(
                        String.format("Reservation with id=%s not found", reservationId)));
    }

    public Reservation cancel(String reservationId) {
        Reservation reservation = reservationRepository.findByReservationId(reservationId).orElseThrow(() ->
                new ReservationNotFoundException(String.format("Reservation with id=%s not found", reservationId)));
        if (Reservation.Status.CANCELLED.equals(reservation.getStatus())) {
            return reservation;
        } else {
            reservation.cancel();
            return reservationRepository.save(reservation);
        }
    }

    public ReservationDifference determineReservationDelta(LocalDate oldStartDate, LocalDate oldEndDate,
                                                           LocalDate newStartDate, LocalDate newEndDate) {
        List<LocalDate> datesToRemove;
        List<LocalDate> datesToAdd;

        if (oldEndDate.isBefore(newStartDate) || newEndDate.isBefore(oldStartDate)) {
            datesToRemove = oldStartDate.datesUntil(oldEndDate.plusDays(1)).toList();
            datesToAdd = newStartDate.datesUntil(newEndDate.plusDays(1)).toList();
        } else {
            List<LocalDate> oldDates = oldStartDate.datesUntil(oldEndDate.plusDays(1)).toList();
            List<LocalDate> newDates = newStartDate.datesUntil(newEndDate.plusDays(1)).toList();
            datesToRemove = oldDates.stream().filter(date -> !newDates.contains(date)).toList();
            datesToAdd = newDates.stream().filter(date -> !oldDates.contains(date)).toList();
        }

        return ReservationDifference.builder()
                .datesToRemove(datesToRemove)
                .datesToAdd(datesToAdd)
                .build();
    }

}
