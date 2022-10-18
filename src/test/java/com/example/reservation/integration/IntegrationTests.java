package com.example.reservation.integration;

import com.example.reservation.entity.Reservation;
import com.example.reservation.model.AvailabilityResponse;
import com.example.reservation.model.ReservationRequest;
import com.example.reservation.model.ReservationResponse;
import com.example.reservation.repository.AvailabilityRepository;
import com.example.reservation.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    AvailabilityRepository availabilityRepository;

    @LocalServerPort
    private int port;

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String LOCATION_ID_HEADER = "locationId";
    private static final String LOCATION_ID = "ISL_VOLC_PO";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@email.com";

    private ReservationResponse reservationResponse;

    @BeforeAll
    @AfterAll
    public void dbReset() {
        availabilityRepository.findAll().forEach(availability -> {
            availability.setReservationId(null);
            availabilityRepository.save(availability);
        });
        reservationRepository.deleteAll();
    }

    @Test
    @Order(1)
    public void fetchAvailability() {

        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        ResponseEntity<AvailabilityResponse> response = testRestTemplate.exchange(
                UriComponentsBuilder.fromUriString(uriInit() + "/availability")
                        .queryParam(LOCATION_ID_HEADER, LOCATION_ID)
                        .build().encode().toUri(),
                HttpMethod.GET, entity, AvailabilityResponse.class);

        AvailabilityResponse expectedAvailability = AvailabilityResponse.builder()
                .locationId(LOCATION_ID)
                .availableDates(defaultAvailability())
                .build();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(expectedAvailability, response.getBody());
    }

    @Test
    @Order(2)
    public void placeReservation() {

        LocalDate arrival = LocalDate.now().plusDays(2);
        LocalDate departure = arrival.plusDays(1);
        ReservationRequest reservationRequest = ReservationRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .locationId(LOCATION_ID)
                .arrival(arrival)
                .departure(departure)
                .build();

        HttpEntity<ReservationRequest> entity = new HttpEntity<>(reservationRequest, new HttpHeaders());

        ResponseEntity<ReservationResponse> response = testRestTemplate.exchange(
                UriComponentsBuilder.fromUriString(uriInit() + "/reservations")
                        .queryParam(LOCATION_ID_HEADER, LOCATION_ID)
                        .build().encode().toUri(),
                HttpMethod.POST, entity, ReservationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        reservationResponse = response.getBody();
        Assertions.assertAll("placeReservation - Validate reservation response",
            () -> Assertions.assertEquals(FIRST_NAME, reservationResponse.getFirstName()),
            () -> Assertions.assertEquals(LAST_NAME, reservationResponse.getLastName()),
            () -> Assertions.assertEquals(EMAIL, reservationResponse.getEmail()),
            () -> Assertions.assertEquals(LOCATION_ID, reservationResponse.getLocationId()),
            () -> Assertions.assertEquals(arrival, reservationResponse.getArrival()),
            () -> Assertions.assertEquals(departure, reservationResponse.getDeparture()),
            () -> Assertions.assertEquals(Reservation.Status.CONFIRMED, reservationResponse.getStatus()));
    }

    @Test
    @Order(3)
    public void fetchAvailabilityAfterReservationPlacement() {

        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        ResponseEntity<AvailabilityResponse> response = testRestTemplate.exchange(
                UriComponentsBuilder.fromUriString(uriInit() + "/availability")
                        .queryParam(LOCATION_ID_HEADER, LOCATION_ID)
                        .build().encode().toUri(),
                HttpMethod.GET, entity, AvailabilityResponse.class);

        List<LocalDate> reservationDates = reservationResponse.getArrival()
                .datesUntil(reservationResponse.getDeparture().plusDays(1)).toList();

        AvailabilityResponse expectedAvailability = AvailabilityResponse.builder()
                .locationId(LOCATION_ID)
                .availableDates(defaultAvailability().stream().filter(date -> !reservationDates.contains(date)).toList())
                .build();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(expectedAvailability, response.getBody());
    }

    @Test
    @Order(4)
    public void fetchReservation() {

        HttpEntity<ReservationRequest> entity = new HttpEntity<>(null, new HttpHeaders());

        ResponseEntity<ReservationResponse> response = testRestTemplate.exchange(
                UriComponentsBuilder.fromUriString(uriInit() + "/reservations/" +
                                reservationResponse.getReservationId()).build().encode().toUri(),
                HttpMethod.GET, entity, ReservationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(reservationResponse, response.getBody());
    }

    @Test
    @Order(5)
    public void updateReservation() {

        LocalDate arrival = LocalDate.now().plusDays(3);
        LocalDate departure = arrival.plusDays(1);
        ReservationRequest reservationRequest = ReservationRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .locationId(LOCATION_ID)
                .arrival(arrival)
                .departure(departure)
                .build();

        HttpEntity<ReservationRequest> entity = new HttpEntity<>(reservationRequest, new HttpHeaders());

        ResponseEntity<ReservationResponse> response = testRestTemplate.exchange(
                UriComponentsBuilder.fromUriString(uriInit() + "/reservations/"
                                + reservationResponse.getReservationId()).build().encode().toUri(),
                HttpMethod.PUT, entity, ReservationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        reservationResponse = response.getBody();
        Assertions.assertAll("updateReservation - Validate reservation response",
                () -> Assertions.assertEquals(FIRST_NAME, reservationResponse.getFirstName()),
                () -> Assertions.assertEquals(LAST_NAME, reservationResponse.getLastName()),
                () -> Assertions.assertEquals(EMAIL, reservationResponse.getEmail()),
                () -> Assertions.assertEquals(LOCATION_ID, reservationResponse.getLocationId()),
                () -> Assertions.assertEquals(arrival, reservationResponse.getArrival()),
                () -> Assertions.assertEquals(departure, reservationResponse.getDeparture()),
                () -> Assertions.assertEquals(Reservation.Status.CONFIRMED, reservationResponse.getStatus()));
    }

    @Test
    @Order(6)
    public void fetchAvailabilityAfterReservationUpdate() {

        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        ResponseEntity<AvailabilityResponse> response = testRestTemplate.exchange(
                UriComponentsBuilder.fromUriString(uriInit() + "/availability")
                        .queryParam(LOCATION_ID_HEADER, LOCATION_ID)
                        .build().encode().toUri(),
                HttpMethod.GET, entity, AvailabilityResponse.class);

        List<LocalDate> reservationDates = reservationResponse.getArrival()
                .datesUntil(reservationResponse.getDeparture().plusDays(1)).toList();

        AvailabilityResponse expectedAvailability = AvailabilityResponse.builder()
                .locationId(LOCATION_ID)
                .availableDates(defaultAvailability().stream().filter(date -> !reservationDates.contains(date)).toList())
                .build();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(expectedAvailability, response.getBody());
    }

    @Test
    @Order(7)
    public void cancelReservation() {

        HttpEntity<ReservationRequest> entity = new HttpEntity<>(null, new HttpHeaders());

        ResponseEntity<ReservationResponse> response = testRestTemplate.exchange(
                UriComponentsBuilder.fromUriString(uriInit() + "/reservations/"
                                + reservationResponse.getReservationId()).build().encode().toUri(),
                HttpMethod.DELETE, entity, ReservationResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        reservationResponse = response.getBody();
        Assertions.assertEquals(Reservation.Status.CANCELLED, reservationResponse.getStatus());
    }

    @Test
    @Order(8)
    public void fetchAvailabilityAfterReservationCancelled() {

        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        ResponseEntity<AvailabilityResponse> response = testRestTemplate.exchange(
                UriComponentsBuilder.fromUriString(uriInit() + "/availability")
                        .queryParam(LOCATION_ID_HEADER, LOCATION_ID)
                        .build().encode().toUri(),
                HttpMethod.GET, entity, AvailabilityResponse.class);

        AvailabilityResponse expectedAvailability = AvailabilityResponse.builder()
                .locationId(LOCATION_ID)
                .availableDates(defaultAvailability())
                .build();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(expectedAvailability, response.getBody());
    }

    @Test
    @Order(9)
    public void parallelReservations() {

        LocalDate arrival = LocalDate.now().plusDays(2);
        List<ResponseEntity<ReservationResponse>> successfulReservationList = arrival.datesUntil(arrival.plusDays(7))
                .map(departure -> ReservationRequest.builder()
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .email(EMAIL)
                        .locationId(LOCATION_ID)
                        .arrival(arrival)
                        .departure(departure)
                        .build())
                .map(reservationRequest -> new HttpEntity<>(reservationRequest, new HttpHeaders()))
                .parallel()
                .map(entity -> testRestTemplate.exchange(
                        UriComponentsBuilder.fromUriString(uriInit() + "/reservations")
                                .queryParam(LOCATION_ID_HEADER, LOCATION_ID).build().encode().toUri(),
                        HttpMethod.POST, entity, ReservationResponse.class))
                .filter(entity -> HttpStatus.OK.equals(entity.getStatusCode()))
                        .toList();


        Assertions.assertEquals(1L, successfulReservationList.size());
        reservationResponse = successfulReservationList.get(0).getBody();
    }

    private String uriInit() {
        return "http://localhost:" + port;
    }

    private List<LocalDate> defaultAvailability() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return tomorrow.datesUntil(tomorrow.plusMonths(1)).toList();
    }
}
