package staryhroft.templog.exception.business;

public class CityAlreadyFavoriteException extends RuntimeException{
    public CityAlreadyFavoriteException(String cityName){
        super("Город " + cityName + " уже находится в избранном");
    }
}
