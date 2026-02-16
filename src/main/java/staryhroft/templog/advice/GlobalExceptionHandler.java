package staryhroft.templog.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import staryhroft.templog.exception.CityNotFavoriteException;
import staryhroft.templog.exception.CityNotFoundException;
import staryhroft.templog.exception.FavoritesLimitExceededException;
import staryhroft.templog.exception.WeatherApiCityNotFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WeatherApiCityNotFoundException.class)
    public ResponseEntity<Object> handleWeatherApiCityNotFound(WeatherApiCityNotFoundException ex){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FavoritesLimitExceededException.class)
    public ResponseEntity<Object> handleFavoritesLimitExceeded(FavoritesLimitExceededException ex){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<Object> handleCityNotFound(CityNotFoundException ex){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CityNotFavoriteException.class)
    public ResponseEntity<Object> handleCityNotFavorite(CityNotFavoriteException ex){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body,HttpStatus.BAD_REQUEST);
    }

}
