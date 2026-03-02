package staryhroft.templog.service.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import staryhroft.templog.client.WeatherApiIntegration;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.repository.CityRepository;
import staryhroft.templog.repository.CityTemperatureRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherUpdateService {

    private final CityRepository cityRepository;
    private final CityTemperatureRepository temperatureRepository;
    private final WeatherApiIntegration weatherApi;

    @Transactional
    public void updateWeatherForAllCities() {
        List<City> cities = cityRepository.findAll();
        log.info("Updating weather for {} cities", cities.size());
        for (City city : cities) {
            try {
                Double currentTemp = weatherApi.getCurrentTemperature(city.getName());
                saveTemperature(city, currentTemp);
            } catch (Exception e) {
                log.error("Failed to update weather for city: {}", city.getName(), e);
                // Можно пробросить исключение или продолжить с другими городами
            }
        }
    }

    private void saveTemperature(City city, Double temperature) {
        CityTemperature record = CityTemperature.builder()
                .city(city)
                .temperature(temperature)
                .timestamp(LocalDateTime.now())
                .build();
        temperatureRepository.save(record);
    }
}
