package staryhroft.templog.exception.business;

public class CityNotFavoriteException extends RuntimeException{
    public CityNotFavoriteException(String cityName){

        super("Город " + cityName + " отсутствует в списке избранных городов");
    }
}
