package staryhroft.templog.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import staryhroft.templog.exception.business.CityNotFavoriteException;
import staryhroft.templog.exception.business.CityNotFoundException;
import staryhroft.templog.exception.business.FavoritesLimitExceededException;
import staryhroft.templog.exception.business.WeatherApiCityNotFoundException;
import staryhroft.templog.exception.message.NoHandlerFoundErrorMessageBuilder;
import staryhroft.templog.exception.message.ValidationErrorMessageBuilder;
import staryhroft.templog.exception.validation.InvalidCityNameException;
import staryhroft.templog.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = NoHandlerFoundErrorMessageBuilder.build(ex);
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Ресурс не найден",
                message
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        String message = ValidationErrorMessageBuilder.build(ex);
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Ошибка запроса",
                message
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WeatherApiCityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWeatherApiCityNotFound(WeatherApiCityNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Город не найден во внешнем сервисе",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, status);
    }




    @ExceptionHandler({InvalidCityNameException.class,
            FavoritesLimitExceededException.class,
            CityNotFavoriteException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Ошибка запроса",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler({CityNotFoundException.class,
            WeatherApiCityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Ресурс не найден",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, status);
    }


}
