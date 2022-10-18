package com.example.reservation.repository;

import com.example.reservation.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    @Query(value = "SELECT * FROM availability where location_id=?1 AND reservation_date IN ?2 " +
            "AND reservation_id IS NULL FOR UPDATE", nativeQuery = true)
    List<Availability> findAllByLocationIdAndReservationDateIn(String locationId, List<LocalDate> reservationDateList);

    @Query(value = "SELECT * FROM availability where location_id=?1 AND reservation_date IN ?2 FOR UPDATE",
            nativeQuery = true)
    List<Availability> findAllToUpdate(String locationId, List<LocalDate> dateList);

    @Query(value = "SELECT * FROM availability where reservation_id=?1 FOR UPDATE",
            nativeQuery = true)
    List<Availability> findAllToUpdate(String reservationId);
    @Query(value = "SELECT * FROM availability WHERE location_id=?1 ORDER BY reservation_date DESC LIMIT 1",
            nativeQuery = true)
    Optional<Availability> findLatestAvailabilityForLocation(String locationId);

    @Query(value = "SELECT * FROM availability WHERE location_id=?1 AND reservation_id IS NULL " +
            "AND reservation_date >= ?2 AND reservation_date <= ?3 ORDER BY reservation_date", nativeQuery = true)
    List<Availability> findAllFreeAvailability(String locationId, LocalDate startDate,
                                               LocalDate endDate);

    @Query(value = "SELECT * FROM availability WHERE location_id=?1 AND reservation_id IS NULL " +
            "AND reservation_date IN ?2 FOR UPDATE", nativeQuery = true)
    List<Availability> findAllFreeAvailabilityToReserve(String locationId, List<LocalDate> dateList);
}
