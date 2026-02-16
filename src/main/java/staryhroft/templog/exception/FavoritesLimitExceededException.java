package staryhroft.templog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FavoritesLimitExceededException extends RuntimeException {
    public FavoritesLimitExceededException(String message) {

        super(message);
    }
}
