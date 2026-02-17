package staryhroft.templog.exception.business;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException(String cityName) {

        super("Город " + cityName + " не найден");
    }
}
