package staryhroft.templog.exception.external;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WeatherApiCityNotFoundException extends RuntimeException {
    public WeatherApiCityNotFoundException(String cityName) {
        super(String.format("Город '%s' не найден в сервисе погоды. " +
                "Проверьте правильность названия.", cityName));
    }
}
