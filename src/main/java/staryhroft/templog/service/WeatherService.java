package staryhroft.templog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import staryhroft.templog.client.WeatherApiClient;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.entity.enums.FavoriteStatus;
import staryhroft.templog.repository.CityRepository;
import staryhroft.templog.repository.CityTemperatureRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {
    private final WeatherApiClient weatherApiClient;
    private final CityRepository cityRepository;
    private final CityTemperatureRepository temperatureRepository;

    @Transactional
    public Double fetchAndSaveTemperature(String cityName){
        Double temperature = weatherApiClient.getCurrentTemperature(cityName);

        City city = cityRepository.findByName(cityName)
                .orElseGet(() -> createNewCity(cityName));

        CityTemperature temperatureRecord  = CityTemperature.builder()
                .city(city)
                .temperature(temperature)
                .build();
        temperatureRepository.save(temperatureRecord);
        log.info("Для города {} сохранена температура: {}", cityName, temperature);
        return temperature;
    }

    private City createNewCity(String cityName){
        City newCity = City.builder()
                .name(cityName)
                .favoriteStatus(FavoriteStatus.NOT_FAVORITE)
                .build();
        return cityRepository.save(newCity);
    }
}
