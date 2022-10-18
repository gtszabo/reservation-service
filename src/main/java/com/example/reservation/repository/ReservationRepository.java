package com.example.reservation.repository;

import com.example.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationId(String reservationId);
    Optional<Reservation> findByReservationIdAndStatus(String reservationId, Reservation.Status status);
}
