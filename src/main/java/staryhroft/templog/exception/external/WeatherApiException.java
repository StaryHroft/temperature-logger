package staryhroft.templog.exception.external;

public class WeatherApiException extends RuntimeException {
    private final String reason;

    public WeatherApiException(String message, String reason){
        super(message);
        this.reason = reason;
    }

    public String getReason(){
        return reason;
    }

    public static WeatherApiException timeout(String cityName){
        return new WeatherApiException("Превышено время ожидания ответа от сервиса погоды. " +
                "Повторите попытку.", "TIMEOUT");
    }

    public static WeatherApiException serverError(String cityName){
        return new WeatherApiException("Сервис погоды временно недоступен (ошибка на стороне сервера). " +
                "Попробуйте позже.", "SERVER_ERROR");
    }

    public static WeatherApiException emptyResponse(String cityName){
        return new WeatherApiException("Сервис погоды вернул некорректный ответ. " +
                "Обратитесь к администратору.", "EMPTY_RESPONSE");
    }

    public static WeatherApiException connectionFailed(String cityName){
        return new WeatherApiException("Не удалось установить соединение с сервисом погоды для города "
                + cityName + ". Проверьте подключение к интернету.", "CONNECTION_FAILED");
    }


}
