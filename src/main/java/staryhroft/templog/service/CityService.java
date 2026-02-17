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
import staryhroft.templog.exception.business.CityNotFoundException;
import staryhroft.templog.exception.business.FavoritesLimitExceededException;
import staryhroft.templog.repository.CityRepository;
import staryhroft.templog.repository.CityTemperatureRepository;

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
    private final WeatherApiIntegration weatherApiIntegration;

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
        City city = findOrCreateCity(cityName);
        Double currentTemp = getCurrentTemperatureForCity(city);
        return buildCityDetailDto(city, currentTemp);
    }

    //Найти или сохранить город
    private City findOrCreateCity(String cityName) {
        return cityRepository.findByName(cityName)
                .orElseGet(() -> {
                    City newCity = City.builder()
                            .name(cityName)
                            .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                            .build();
                    cityRepository.save(newCity);
                    log.info("Создан новый город: {}", cityName);
                    return newCity;
                });
    }

    //Проверка температуры
    private Double getCurrentTemperatureForCity(City city) {
        Optional<CityTemperature> lastTempOpt = temperatureRepository.findFirstByCityOrderByTimestampDesc(city);

        //Проверка актуальности
        if (lastTempOpt.isPresent()) {
            LocalDateTime lastTime = lastTempOpt.get().getTimestamp();
            if (lastTime.isAfter(LocalDateTime.now().minusDays(1))) {
                log.debug("Температура для города {} актуальна", city.getName());
                return lastTempOpt.get().getTemperature();
            }
        }
        //Если нет данных или они устарели - запрос из АПИ и сохранение
        Double freshTemp = weatherApiIntegration.getCurrentTemperature(city.getName());
        CityTemperature newRecord = CityTemperature.builder()
                .city(city)
                .temperature(freshTemp)
                .build();
        temperatureRepository.save(newRecord);
        log.info("Получена и сохранена новая температура для {}", city.getName());
        return freshTemp;
    }

    //Создание ДТО
    private CityDetailDto buildCityDetailDto(City city, Double currentTemp) {
        CityTemperature latest = temperatureRepository
                .findFirstByCityOrderByTimestampDesc(city)
                .orElseThrow(() -> new IllegalStateException("Температура не найдена"));

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

        if (city.getFavoriteStatus() == FavoriteStatus.FAVORITE) {
            log.debug("Город {} уже в избранном", cityName);
            return;
        }

        long favoriteCount = cityRepository.countByFavoriteStatus(FavoriteStatus.FAVORITE);
        if (favoriteCount >= 3) {
            throw new FavoritesLimitExceededException(
                    "Нельзя добавить больше 3 избранных городов. Удалите один из текущих избранных");
        }

        city.setFavoriteStatus(FavoriteStatus.FAVORITE);
        cityRepository.save(city);
        log.info("Город {} добавлен в избранное", cityName);
    }

    //Удалить город из избранного
    @Transactional
    public void unmarkFromFavorite(String cityName) {
        City city = cityRepository.findByName(cityName)
                .orElseThrow(() -> new CityNotFoundException(cityName));

        if (city.getFavoriteStatus() == FavoriteStatus.NOT_FAVORITE) {
            throw new FavoritesLimitExceededException("Город " + cityName + " не находится в избранном");
        }
        city.setFavoriteStatus(FavoriteStatus.NOT_FAVORITE);
        cityRepository.save(city);
        log.info("Город {} удалён из избранного", cityName);
    }
}
