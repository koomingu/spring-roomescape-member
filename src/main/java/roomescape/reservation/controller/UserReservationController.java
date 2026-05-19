package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
@Validated
public class UserReservationController {
    private final ReservationService reservationService;
    private final HttpMessageConverters messageConverters;

    public UserReservationController(ReservationService reservationService, HttpMessageConverters messageConverters) {
        this.reservationService = reservationService;
        this.messageConverters = messageConverters;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @RequestParam(required = true)
            @NotBlank(message = "조회할 예약자 이름은 필수입니다.")
            @Pattern(regexp = "^[^<>]*$", message = "올바르지 않은 이름 형식입니다.")
            String name
    ) {
        List<Reservation> reservations = reservationService.findAllByName(name);

        List<ReservationResponse> response = reservations.stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest reservationRequest) {
        Reservation reservation = reservationService.save(
                reservationRequest.name(),
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId()
        );

        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable long id,
            @RequestParam(required = true)
            @NotBlank(message = "이름은 비어있을 수 없습니다.")
            String name
    ) {
        reservationService.deleteByUser(id, name);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable long id,
            @Valid @RequestBody ReservationUpdateRequest updateRequest){

        Reservation updateReservation = reservationService.updateReservationDateTimeByUser(
                id,
                updateRequest.name(),
                updateRequest.date(),
                updateRequest.timeId()
        );

        return ResponseEntity.ok(ReservationResponse.from(updateReservation));
    }
}
