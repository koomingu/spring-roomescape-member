package roomescape.reservation.domain;

import java.time.LocalTime;
import roomescape.exception.BadRequestException;
import roomescape.exception.UnauthorizedActionException;
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
        validateReservationDateTime(date, time.startAt());

        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public void validateUpdateDateTime(LocalDate newDate, LocalTime newTime) {
        validateReservationDateTime(newDate, newTime);
    }

    public void validateOwner(String userName) {
        if (!this.name.equals(userName)) {
            throw new UnauthorizedActionException("예약자 이름이 일치하지 않습니다.");
        }
    }

    public void validateDeletable() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (date.isBefore(today) || (date.equals(today) && time.startAt().isBefore(now))) {
            throw new BadRequestException("지난 예약은 삭제할 수 없습니다.");
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
