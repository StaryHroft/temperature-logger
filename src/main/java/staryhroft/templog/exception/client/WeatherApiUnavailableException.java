package staryhroft.templog.exception.client;

public class WeatherApiUnavailableException extends RuntimeException {
    public WeatherApiUnavailableException(String message) {
        super(message);
    }
}
