package staryhroft.templog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import staryhroft.templog.client.WeatherApiIntegration;
import staryhroft.templog.dto.ApiResponse;
import staryhroft.templog.dto.CityDetailDto;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.exception.business.CityAlreadyFavoriteException;
import staryhroft.templog.exception.business.CityNotFavoriteException;
import staryhroft.templog.exception.business.CityNotFoundException;
import staryhroft.templog.exception.business.FavoritesLimitExceededException;
import staryhroft.templog.repository.CityRepository;
import staryhroft.templog.repository.CityTemperatureRepository;
import staryhroft.templog.service.city.CityFinder;
import staryhroft.templog.service.city.CityTempetratureUpdater;
import staryhroft.templog.service.favorite.FavoriteManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CityService {
    private final CityRepository cityRepository;
    private final CityTemperatureRepository temperatureRepository;
    private final CityFinder cityFinder;
    private final CityTempetratureUpdater tempetratureUpdater;
    private final FavoriteManager favoriteManager;

    //Получить список городов
    public ApiResponse<List<CityDetailDto>> getAllCities() {
        List<City> cities = Stream.concat(
                        cityRepository.findByFavoriteStatusOrderByIdDesc(FavoriteStatus.FAVORITE).stream(),
                        cityRepository.findByFavoriteStatusOrderByIdDesc(FavoriteStatus.NOT_FAVORITE).stream()
                ).collect(Collectors.toList());
        List<CityDetailDto> cityDtos = cities.stream()
                .map(city -> {
                    CityTemperature lastTemp = temperatureRepository
                            .findFirstByCityOrderByTimestampDesc(city)
                            .orElse(null);
                    Double temperature = lastTemp != null ? lastTemp.getTemperature() : null;
                    LocalDateTime time = lastTemp != null ? lastTemp.getTimestamp() : null;
                    return new CityDetailDto(
                            city.getName(),
                            city.getFavoriteStatus(),
                            temperature,
                            time
                    );
                })
                .collect(Collectors.toList());

        String message = cityDtos.isEmpty() ? "Список городов пуст" : null;
        return new ApiResponse<>(LocalDateTime.now(), message, cityDtos);

    }

    //Получить сведения о городе по его названию
    @Transactional
    public CityDetailDto getOrUpdateCity(String cityName) {
        City city = cityFinder.findOrCreate(cityName);
        tempetratureUpdater.getFreshTemperature(city);
        CityTemperature latest = tempetratureUpdater.getLatestRecord(city);
        return new CityDetailDto(
                city.getName(),
                city.getFavoriteStatus(),
                latest.getTemperature(),
                latest.getTimestamp()
                );
    }

    //Получить количество городов
    public long getCityCount() {
        return cityRepository.count();
    }

    //Удалить город
    @Transactional
    public void deleteCity(String cityName) {
        City city = cityRepository.findByName(cityName)
                .orElseThrow(() -> new CityNotFoundException(cityName));
        cityRepository.delete(city);
        log.info("Город {} удален", cityName);
    }

    //Удалить все города
    @Transactional
    public void deleteAllCities() {
        cityRepository.deleteAll();
    }

    //Добавить город в избранное
    @Transactional
    public void markAsFavorite(String cityName) {
        City city = cityRepository.findByName(cityName)
                .orElseThrow(() -> new CityNotFoundException(cityName));
        favoriteManager.addToFavorites(city);
    }

    //Удалить город из избранного
    @Transactional
    public void unmarkFromFavorite(String cityName) {
        City city = cityRepository.findByName(cityName)
                .orElseThrow(() -> new CityNotFoundException(cityName));

        favoriteManager.removeFromFavorites(city);
    }
}
