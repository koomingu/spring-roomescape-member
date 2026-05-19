package roomescape.reservation.domain;

import java.time.LocalTime;
import roomescape.exception.BadRequestException;
import roomescape.exception.ForbiddenActionException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;

public class Reservation {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        this(null, name, date, time, theme);
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        validateNotNull(name, date, time, theme);
        validateNameNotBlank(name);
        validateReservationDateTime(date, time.startAt());

        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public void validateUpdateDateTime(LocalDate newDate, LocalTime newTime) {
        if (newDate == null || newTime == null) {
            throw new BadRequestException("변경할 날짜와 시간 정보가 필요합니다.");
        }
        validateReservationDateTime(newDate, newTime);
    }

    public void validateOwner(String userName) {
        if (!this.name.equals(userName)) {
            throw new ForbiddenActionException("예약자 이름이 일치하지 않습니다.");
        }
    }

    public void validateDeletable() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isBefore(today) || (date.equals(today) && time.startAt().isBefore(now))) {
            throw new BadRequestException("지난 예약은 삭제할 수 없습니다.");
        }
    }

    private void validateNotNull(String name, LocalDate date, ReservationTime time, Theme theme) {
        if (name == null || date == null || time == null || theme == null) {
            throw new BadRequestException("예약의 필수 정보(이름, 날짜, 시간, 테마)는 누락될 수 없습니다.");
        }
    }

    private void validateNameNotBlank(String name) {
        if (name.isBlank()) {
            throw new BadRequestException("예약자 이름은 비어있거나 공백일 수 없습니다.");
        }
    }

    private void validateReservationDateTime(LocalDate date, LocalTime time) {
        LocalDate today = LocalDate.now();

        if (date.isBefore(today)) {
            throw new BadRequestException("예약 날짜는 오늘 이후여야 합니다.");
        }
        if (date.equals(today) && time.isBefore(LocalTime.now())) {
            throw new BadRequestException("예약 시간은 현재 시간 이후여야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation other)) {
            return false;
        }
        if (this.id == null || other.id == null) {
            return false;
        }
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return System.identityHashCode(this);
        }
        return id.hashCode();
    }
}
