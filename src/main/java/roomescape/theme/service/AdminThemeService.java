package roomescape.theme.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.DuplicateException;
import roomescape.exception.ThemeInUseException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class AdminThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public AdminThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Theme save(String name, String description, String thumbnail) {
        try {
            return themeRepository.save(name, description, thumbnail);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("같은 이름의 테마가 존재합니다.");
        }

    }

    @Transactional
    public void delete(long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ThemeInUseException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeRepository.delete(id);
    }
}
