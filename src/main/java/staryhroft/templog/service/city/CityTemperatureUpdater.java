package staryhroft.templog.service.city;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import staryhroft.templog.client.WeatherApiIntegration;
import staryhroft.templog.entity.City;
import staryhroft.templog.entity.CityTemperature;
import staryhroft.templog.repository.CityTemperatureRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CityTemperatureUpdater {
    private final CityTemperatureRepository temperatureRepository;
    private final WeatherApiIntegration weatherApiIntegration;

    //Проверка температуры
    public Double getFreshTemperature(City city) {
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
        Double newTemp = weatherApiIntegration.getCurrentTemperature(city.getName());
        CityTemperature newRecord = CityTemperature.builder()
                .city(city)
                .temperature(newTemp)
                .build();
        temperatureRepository.save(newRecord);
        log.info("Получена и сохранена новая температура для {}", city.getName());
        return newTemp;
    }

    public CityTemperature getLatestRecord(City city) {
        return temperatureRepository.findFirstByCityOrderByTimestampDesc(city)
                .orElseThrow(() -> new IllegalStateException("Температура не найдена для города " + city.getName()));
    }
}
