package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.BadRequestException;
import roomescape.exception.UnauthorizedActionException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@DisplayName("Reservation 도메인 단위 테스트")
class ReservationTest {

    private Theme theme;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        theme = new Theme(1L, "호러 테마", "무서운 방탈출", "thumbnail.jpg");
        time = mock(ReservationTime.class);
    }

    @Nested
    @DisplayName("예약 생성 및 시간 유효성 검증 (validateReservationDateTime / validateUpdateDateTime)")
    class DateTimeValidationTest {

        @Test
        @DisplayName("내일 날짜의 유효한 시간으로 예약을 생성하면 예외가 발생하지 않는다.")
        void 내일_날짜로_예약을_생성하면_성공한다() {
            // given
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            given(time.startAt()).willReturn(LocalTime.of(14, 0));

            // when & then
            assertThatCode(() -> new Reservation("홍길동", tomorrow, time, theme))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("과거 날짜로 예약을 생성하려고 하면 BadRequestException 예외가 발생한다.")
        void 과거_날짜로_예약을_생성하면_예외가_발생한다() {
            // given
            LocalDate yesterday = LocalDate.now().minusDays(1);
            given(time.startAt()).willReturn(LocalTime.of(14, 0));

            // when & then
            assertThatThrownBy(() -> new Reservation("홍길동", yesterday, time, theme))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약 날짜는 오늘 이후여야 합니다.");
        }

        @Test
        @DisplayName("오늘 날짜지만 이미 지나간 시간으로 예약을 생성하려고 하면 BadRequestException 예외가 발생한다.")
        void 오늘_날짜지만_과거_시간으로_예약을_생성하면_예외가_발생한다() {
            // given
            LocalDate today = LocalDate.now();
            LocalTime pastTime = LocalTime.now().minusHours(1); // 1시간 전
            given(time.startAt()).willReturn(pastTime);

            // when & then
            assertThatThrownBy(() -> new Reservation("홍길동", today, time, theme))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약 시간은 현재 시간 이후여야 합니다.");
        }

        @Test
        @DisplayName("예약을 수정할 때 과거 날짜로 변경하려고 하면 예외가 발생한다.")
        void 과거_날짜로_예약을_수정하면_예외가_발생한다() {
            // given
            LocalDate validDate = LocalDate.now().plusDays(1);
            given(time.startAt()).willReturn(LocalTime.now().plusHours(1));
            Reservation reservation = new Reservation("홍길동", validDate, time, theme);

            LocalDate pastDate = LocalDate.now().minusDays(1);
            LocalTime anyTime = LocalTime.of(12, 0);

            // when & then
            assertThatThrownBy(() -> reservation.validateUpdateDateTime(pastDate, anyTime))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약 날짜는 오늘 이후여야 합니다.");
        }
    }

    @Nested
    @DisplayName("예약자 본인 여부 검증 (validateOwner)")
    class OwnerValidationTest {

        private Reservation reservation;

        @BeforeEach
        void setUp() {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            given(time.startAt()).willReturn(LocalTime.of(12, 0));
            reservation = new Reservation("홍길동", futureDate, time, theme);
        }

        @Test
        @DisplayName("예약자 이름이 정확히 일치하면 예외가 발생하지 않는다.")
        void 예약자_이름이_일치하면_예외가_발생하지_않는다() {
            // given
            String matchName = "홍길동";

            // when & then
            assertThatCode(() -> reservation.validateOwner(matchName))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("예약자 이름이 일치하지 않으면 UnauthorizedActionException 예외가 발생한다.")
        void 예약자_이름이_일치하지_않으면_예외가_발생한다() {
            // given
            String mismatchName = "김철수";

            // when & then
            assertThatThrownBy(() -> reservation.validateOwner(mismatchName))
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessage("예약자 이름이 일치하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("예약 삭제 가능 여부 판단 검증 (validateDeletable)")
    class DeletableValidationTest {

        @Test
        @DisplayName("아직 다가오지 않은 미래의 예약은 삭제할 수 있다.")
        void 미래의_예약은_삭제가_가능하다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(2);
            given(time.startAt()).willReturn(LocalTime.of(15, 0));
            Reservation reservation = new Reservation("홍길동", futureDate, time, theme);

            // when & then
            assertThatCode(reservation::validateDeletable)
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("이미 날짜가 지난 과거의 예약은 삭제할 수 없다.")
        void 과거의_예약을_삭제하려_하면_예외가_발생한다() {
            // given
            LocalDate today = LocalDate.now();
            LocalTime futureTime = LocalTime.now().plusHours(2);

            given(time.startAt()).willReturn(futureTime);
            Reservation reservation = new Reservation("홍길동", today, time, theme);

            LocalTime pastTime = LocalTime.now().minusHours(1);
            given(time.startAt()).willReturn(pastTime);

            // when & then
            assertThatThrownBy(reservation::validateDeletable)
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("지난 예약은 삭제할 수 없습니다.");
        }
    }
}
