package staryhroft.templog.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import staryhroft.templog.client.WeatherApiIntegration;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.repository.CityRepository;
import staryhroft.templog.repository.CityTemperatureRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor@Slf4j
public class WeatherUpdateScheduler {
    private final CityRepository cityRepository;
    private final WeatherApiIntegration weatherApiIntegration;
    private final CityTemperatureRepository temperatureRepository;

    @Scheduled(cron = "0 0 */6 * * *", zone = "Europe/Moscow")
    public void updateWeatherForAllCities(){
        List<City> cities = cityRepository.findAll();
        if (cities.isEmpty()){
            log.info("Нет городов для обновления погоды");
            return;
        }
        log.info("Начало обновления погоды для {} городов", cities.size());
        for (City city : cities){
            try {
                Double temperature = weatherApiIntegration.getCurrentTemperature(city.getName());
                saveTemperature(city, temperature);
                log.debug("Успешно обновлена погода для города {}: {}°C", city.getName(), temperature);
            } catch (Exception e){
                log.error("", city.getName(), e.getMessage());
            }
        }
        log.info("Плановое обновление погоды завершено");
    }
    private void saveTemperature(City city, Double temperature){
        CityTemperature cityTemperature = new CityTemperature();
        cityTemperature.setCity(city);
        cityTemperature.setTemperature(temperature);
        cityTemperature.setTimestamp(LocalDateTime.now());
        temperatureRepository.save(cityTemperature);
    }
}
