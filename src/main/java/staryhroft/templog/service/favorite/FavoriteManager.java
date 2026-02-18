package staryhroft.templog.service.favorite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.exception.business.CityAlreadyFavoriteException;
import staryhroft.templog.exception.business.CityNotFavoriteException;
import staryhroft.templog.exception.business.CityNotFoundException;
import staryhroft.templog.exception.business.FavoritesLimitExceededException;
import staryhroft.templog.repository.CityRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class FavoriteManager {

    private static final int MAX_FAVORITES = 3;
    private final CityRepository cityRepository;


    public void addToFavorites(City city) {
        if (city.getFavoriteStatus() == FavoriteStatus.FAVORITE) {
            log.debug("Город {} уже в избранном", city.getName());
            throw new CityAlreadyFavoriteException(city.getName());
        }
        long favoriteCount = cityRepository.countByFavoriteStatus(FavoriteStatus.FAVORITE);
        if (favoriteCount >= MAX_FAVORITES) {
            throw new FavoritesLimitExceededException(city.getName());
        }
        city.setFavoriteStatus(FavoriteStatus.FAVORITE);
        cityRepository.save(city);
        log.info("Город {} добавлен в избранное", city.getName());
    }

    public void removeFromFavorites(City city){
        if (city.getFavoriteStatus() == FavoriteStatus.NOT_FAVORITE) {
            throw new CityNotFavoriteException(city.getName());
        }
        city.setFavoriteStatus(FavoriteStatus.NOT_FAVORITE);
        cityRepository.save(city);
        log.info("Город {} удалён из избранного", city.getName());
    }

    public boolean isFavorite(City city){
        return  city.getFavoriteStatus() == FavoriteStatus.FAVORITE;
    }
}
