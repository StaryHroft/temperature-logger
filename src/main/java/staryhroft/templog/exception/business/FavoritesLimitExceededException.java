package staryhroft.templog.exception.business;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class FavoritesLimitExceededException extends RuntimeException {
    public FavoritesLimitExceededException(String cityName) {

        super("Невозможно добавить город " + cityName + " в избранное: достигнут лимит (максимум 3 города).");
    }
}
