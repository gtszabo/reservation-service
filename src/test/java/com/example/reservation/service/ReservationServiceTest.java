package com.example.reservation.service;

import com.example.reservation.entity.Reservation;
import com.example.reservation.exception.ReservationNotFoundException;
import com.example.reservation.model.ReservationDifference;
import com.example.reservation.repository.ReservationRepository;
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
import java.util.Optional;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class ReservationServiceTest {

    Random random = new Random();

    @InjectMocks
    ReservationService reservationService;

    @Mock
    ReservationRepository reservationRepository;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void retrieve_reservationNotFound() {
        Mockito.when(reservationRepository.findByReservationIdAndStatus(anyString(), any(Reservation.Status.class)))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ReservationNotFoundException.class, () ->
                reservationService.retrieve("DUMMY"));
    }

    @Test
    public void retrieve_reservationFound() {
        String reservationId = "1234";
        Reservation reservation = Reservation.builder()
                .id(random.nextLong())
                .reservationId(reservationId)
                .build();

        Mockito.when(reservationRepository.findByReservationIdAndStatus(reservationId, Reservation.Status.CONFIRMED))
                .thenReturn(Optional.of(reservation));

        Assertions.assertEquals(reservation, reservationService.retrieve(reservationId));
    }

    @Test
    public void cancel_reservationConfirmedStatus() {
        String reservationId = "1234";
        Reservation reservation = Reservation.builder()
                .id(random.nextLong())
                .reservationId(reservationId)
                .status(Reservation.Status.CONFIRMED)
                .build();
        Mockito.when(reservationRepository.findByReservationId(anyString()))
                .thenReturn(Optional.of(reservation));
        reservation.setStatus(Reservation.Status.CANCELLED);
        Mockito.when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        Assertions.assertEquals(reservation, reservationService.cancel(reservationId));
    }

    @Test
    public void cancel_reservationCancelledStatus() {
        String reservationId = "1234";
        Reservation reservation = Reservation.builder()
                .id(random.nextLong())
                .reservationId(reservationId)
                .status(Reservation.Status.CANCELLED)
                .build();
        Mockito.when(reservationRepository.findByReservationId(anyString()))
                .thenReturn(Optional.of(reservation));

        Assertions.assertEquals(reservation, reservationService.cancel(reservationId));
        Mockito.verify(reservationRepository, Mockito.never()).save(any(Reservation.class));
    }

    @Test
    public void determineReservationDelta_nonOverlappingDates() {
        LocalDate startDate1 = LocalDate.of(2022, 10, 15);
        LocalDate endDate1 = LocalDate.of(2022, 10, 17);
        LocalDate startDate2 = LocalDate.of(2022, 10, 18);
        LocalDate endDate2 = LocalDate.of(2022, 10, 20);

        ReservationDifference expectedDiff = ReservationDifference.builder()
                .datesToRemove(startDate1.datesUntil(endDate1.plusDays(1)).toList())
                .datesToAdd(startDate2.datesUntil(endDate2.plusDays(1)).toList())
                .build();

        Assertions.assertEquals(expectedDiff, reservationService.determineReservationDelta(startDate1, endDate1,
                startDate2, endDate2));
    }

    @Test
    public void determineReservationDelta_singleDateChange() {
        LocalDate startDate1 = LocalDate.of(2022, 10, 15);
        LocalDate endDate1 = LocalDate.of(2022, 10, 15);
        LocalDate startDate2 = LocalDate.of(2022, 10, 18);
        LocalDate endDate2 = LocalDate.of(2022, 10, 18);

        ReservationDifference expectedDiff = ReservationDifference.builder()
                .datesToRemove(Collections.singletonList(startDate1))
                .datesToAdd(Collections.singletonList(startDate2))
                .build();

        Assertions.assertEquals(expectedDiff, reservationService.determineReservationDelta(startDate1, endDate1,
                startDate2, endDate2));
    }

    @Test
    public void determineReservationDelta_earlierDatesWithOverlap() {
        LocalDate startDate1 = LocalDate.of(2022, 10, 15);
        LocalDate endDate1 = LocalDate.of(2022, 10, 17);
        LocalDate startDate2 = LocalDate.of(2022, 10, 14);
        LocalDate endDate2 = LocalDate.of(2022, 10, 16);

        ReservationDifference expectedDiff = ReservationDifference.builder()
                .datesToRemove(List.of(endDate1))
                .datesToAdd(List.of(startDate2))
                .build();

        Assertions.assertEquals(expectedDiff, reservationService.determineReservationDelta(startDate1, endDate1,
                startDate2, endDate2));
    }

    @Test
    public void determineReservationDelta_laterDatesWithOverlap() {
        LocalDate startDate1 = LocalDate.of(2022, 10, 15);
        LocalDate endDate1 = LocalDate.of(2022, 10, 17);
        LocalDate startDate2 = LocalDate.of(2022, 10, 16);
        LocalDate endDate2 = LocalDate.of(2022, 10, 18);

        ReservationDifference expectedDiff = ReservationDifference.builder()
                .datesToRemove(List.of(startDate1))
                .datesToAdd(List.of(endDate2))
                .build();

        Assertions.assertEquals(expectedDiff, reservationService.determineReservationDelta(startDate1, endDate1,
                startDate2, endDate2));
    }

    @Test
    public void determineReservationDelta_shorterReservation() {
        LocalDate startDate1 = LocalDate.of(2022, 10, 15);
        LocalDate endDate1 = LocalDate.of(2022, 10, 17);
        LocalDate startDate2 = LocalDate.of(2022, 10, 15);
        LocalDate endDate2 = LocalDate.of(2022, 10, 16);

        ReservationDifference expectedDiff = ReservationDifference.builder()
                .datesToRemove(List.of(endDate1))
                .datesToAdd(Collections.emptyList())
                .build();

        Assertions.assertEquals(expectedDiff, reservationService.determineReservationDelta(startDate1, endDate1,
                startDate2, endDate2));
    }

    @Test
    public void determineReservationDelta_longerReservation() {
        LocalDate startDate1 = LocalDate.of(2022, 10, 15);
        LocalDate endDate1 = LocalDate.of(2022, 10, 16);
        LocalDate startDate2 = LocalDate.of(2022, 10, 15);
        LocalDate endDate2 = LocalDate.of(2022, 10, 17);

        ReservationDifference expectedDiff = ReservationDifference.builder()
                .datesToRemove(Collections.emptyList())
                .datesToAdd(List.of(endDate2))
                .build();

        Assertions.assertEquals(expectedDiff, reservationService.determineReservationDelta(startDate1, endDate1,
                startDate2, endDate2));
    }

}
