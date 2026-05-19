package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ThemeInUseException extends ApiException {
    public ThemeInUseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
